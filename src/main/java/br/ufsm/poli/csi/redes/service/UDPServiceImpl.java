package br.ufsm.poli.csi.redes.service;

import br.ufsm.poli.csi.redes.model.Mensagem;
import br.ufsm.poli.csi.redes.model.Usuario;
import br.ufsm.poli.csi.redes.swing.ChatClientSwing;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import netscape.javascript.JSObject;
import org.w3c.dom.ls.LSOutput;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * criar DatagramSocket
 * montar DatagramPacket
 * chamar socket.send()
 * receive()
 * broadcast para descobrir usuarios
 * mensagem de sonda afeta usuario listener - tem lista de usuarios na classe
 * */

//mensagem na interface --> chama essa classe
public class UDPServiceImpl implements UDPService {
    //atributos
    private DatagramSocket dtSocket; //socket principal para a rede
//    private final int porta = 8080;
    private int portaOrigem; //escuta
    private int portaDestino; //envia mensagem
    private Usuario usuario = null; //referencia o usuario atual
    private UDPServiceMensagemListener mensagemListener = null; //avisar sobre novas mensagens
    private UDPServiceUsuarioListener usuarioListener = null; //avisar sobre novos usuarios
    private final ObjectMapper objectMapper = new ObjectMapper(); //converter para json e vice versa
    private final Map<Usuario, Long> usuariosOnline = new ConcurrentHashMap<>(); //rastreia usuarios online e quando ficou online pela ultima vez


    //construtor
    public UDPServiceImpl(int portaOrigem, int portaDestino) {
        this.portaOrigem = portaOrigem;
        this.portaDestino = portaDestino;
        try {
            //cria um socket
            this.dtSocket = new DatagramSocket(this.portaOrigem);
            System.out.println("UDPServiceImpl estabeleciado na porta: " + this.portaOrigem);

            //threads
            new Thread(new EnviaSonda()).start();
            new Thread(new EscutaSonda()).start();
            //timeout
//            new Thread(new VerificaTimeouts()).start();

        } catch (SocketException e) {
            throw new RuntimeException("ERRO ao estabelecer serviço UDP", e);
        }

    }


    //thread de enviar sonda (para mostrar usuario na lista dos outros) - atualizada moodle
    private class EnviaSonda implements Runnable {
        @SneakyThrows
        @Override
        public void run() {
//            System.out.println("entrou no run da classe EnviaSonda");
            while (true) {
//                System.out.println("entrou no while true do run da classe EnviaSonda");
                Thread.sleep(5000);
                if (usuario == null) {
                    System.out.println("usuario == null");
                    continue;
                }

                try {
                    //manda sonda
                    Mensagem mensagem = new Mensagem();
                    mensagem.setTipoMensagem(Mensagem.TipoMensagem.sonda);
                    mensagem.setUsuario(usuario.getNome());
                    mensagem.setStatus(usuario.getStatus().toString());

                    //converte para string
                    ObjectMapper mapper = new ObjectMapper();

                    //converte para json
                    String strMensagem = mapper.writeValueAsString(mensagem);

                    //converte para byte
                    byte[] bMensagem = strMensagem.getBytes();

                    //envias para rtodos os IPs da sub rede
                    InetAddress broadcast = InetAddress.getByName("255.255.255.255");
//                    for (int i = 1; i < 255; i++) {
//                        System.out.println("entoru no for ENVIA\n");
                        DatagramPacket pacote = new DatagramPacket(
                                bMensagem, bMensagem.length,
//                                InetAddress.getByName("192.168.83." + i),
                                broadcast,
                                portaDestino //da outra janela
                        );

                        //envia o socket principal da classe
                        dtSocket.send(pacote);
//                    }
                    System.out.println("SONDA enviada para sub rede na porta " + portaDestino);

                } catch (Exception e) {
                    System.out.println("ERRO ao enviar mensagem de SONDA: " + e.getMessage());
                }

//                System.out.println("FIM do while true ENVIA sonda");
            }
        }
    }


    //thread de escutar, espera algo chegar na porta de origem
    private class EscutaSonda implements Runnable {
        @Override
        public void run() {
//            System.out.println("entrou no run da classe ESCUTA Sonda");

            while (true) {
//                System.out.println("entrou no while true do ESCUTA");

                try {
                    //buffer vazio para receber dados da rede
                    byte[] buffer = new byte[4096]; //buffer para json
                    DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);

                    //espera pacote chegar na porta de escuta
                    try {
                        dtSocket.receive(pacoteRecebido);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    //converte bytes para string json
                    String jsonRecebido = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength()); //pega apenas o conteudo da mensagem

                    //converte json para mensagem de volta
                    ObjectMapper mapper = new ObjectMapper();
                    Mensagem msg;
                    try {
                        msg = mapper.readValue(jsonRecebido, Mensagem.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    // objeto Usuario (remetente da msg) - nome e status da msg, endereço IP do pacote
                    Usuario remetente = new Usuario(
                            msg.getUsuario(),
                            Usuario.StatusUsuario.valueOf(msg.getStatus() != null ? msg.getStatus() : "DISPONIVEL"),
                            pacoteRecebido.getAddress()
                    );

                    //ignora mensagens do proprio usuario
                    if (usuario != null && usuario.equals(remetente)) {
                        continue; //pula para proxima iteraçao do loop (ignora)
                    }

                    //identifica tipo da mensagem e chama o listener
                    switch (msg.getTipoMensagem()) {
                        case sonda: //avisa o listener do usuario que um usuari foi adicionado/atualizado
                            if (usuarioListener != null) {
                                usuarioListener.usuarioAdicionado(remetente);
                            }
                            break;

                        case msg_individual: //avsa o listener de mensagem
                            if (mensagemListener != null) {
                                mensagemListener.mensagemRecebida(msg.getMsg(), remetente, false);
                            }
                            break;

                        case msg_grupo: //avsa o listener de mensagem tb
                            if (mensagemListener != null) {
                                mensagemListener.mensagemRecebida(msg.getMsg(), remetente, true);
                            }
                            break;

                        case fim_chat:
                            System.out.println("FIM DE CHAT: " + remetente.getNome());
                            break;

                    }
                } catch (Exception e) {
                    //nao quebra o codigo, continua recebendo mensagens
                    System.out.println("ERRO na thread de escuta: " + e.getMessage());
                }


            }
        }
    }


    @Override
    public void enviarMensagem(String mensagem, Usuario destinatario, boolean chatGeral) {
        System.out.println("função enviarMensagem");
        //cria thread para enviar uma mensagem = manda um pacote e termina
        new Thread(() -> {
            try {
                                        // se chatGeral = true o tipo é msg_grupo; se = false é msg_individual
                Mensagem.TipoMensagem tipo = chatGeral ? Mensagem.TipoMensagem.msg_grupo : Mensagem.TipoMensagem.msg_individual;
                //monta objetp do tipo Mensagem
                Mensagem objMsg = Mensagem.builder().
                        tipoMensagem(tipo).
                        usuario(this.usuario.getNome()).
                        status(this.usuario.getStatus().toString()).
                        msg(mensagem).build();

                //para json
                String jsonMsg = objectMapper.writeValueAsString(objMsg);
                byte[] buffer = jsonMsg.getBytes();

                //endereço de destino
                InetAddress destino;
                int portaFinal;
                if (chatGeral) { //se for chat_geral manda para endereço broadcast
                    destino = InetAddress.getByName("255.255.255.255"); //broadcast
                    portaFinal = this.portaDestino; //manda para porta do outro usuario
                } else {
                    destino = destinatario.getEndereco(); //IP especifico
                    portaFinal = destinatario.getEndereco().equals(this.usuario.getEndereco()) ? this.portaDestino : this.portaOrigem;
                }

                //cria pacote
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, destino, portaFinal);
                //envia pacote
                dtSocket.send(packet);
                System.out.println("Mensagem enviada para " + destino.getHostAddress() + ":" + portaFinal);
            } catch (Exception e) {
                System.out.println("ERRO ao enviar mensagem: " + e.getMessage());
            }
        }).start();
    }


    @Override
    public void usuarioAlterado(Usuario usuario) {
        this.usuario = usuario;
        System.out.println("função usuarioAlterado");
        //chama sempre que mandar uma osnda
    }

    @Override
    public void addListenerMensagem(UDPServiceMensagemListener listener) {
        //"esse eh o meu listener, quando receber mensagem me chama por aqui"
        //listeners para notificar a interface
        this.mensagemListener = listener;
        System.out.println("função addListenerMensagem");
    }

    @Override
    public void addListenerUsuario(UDPServiceUsuarioListener listener) {
        this.usuarioListener = listener;
        System.out.println("função addListenerUsuario");
    }
}
