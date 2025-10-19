
package br.ufsm.poli.csi.redes.service;

import br.ufsm.poli.csi.redes.model.Mensagem;
import br.ufsm.poli.csi.redes.model.Usuario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
// import netscape.javascript.JSObject; // Removido: Não está sendo usado
// import org.w3c.dom.ls.LSOutput; // Removido: Não está sendo usado

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class UDPServiceImpl implements UDPService {
    private final String ipPadrao;
    //atributos
    protected DatagramSocket dtSocket; //socket principal para a rede
    private int portaOrigem; //escuta
    protected int portaDestino; //envia mensagem (porta de escuta padrão para todos os pares)
    protected Usuario usuario = null; //referencia o usuario atual
    private UDPServiceMensagemListener mensagemListener = null; //avisar sobre novas mensagens
    private UDPServiceUsuarioListener usuarioListener = null; //avisar sobre novos usuarios
    protected final ObjectMapper objectMapper = new ObjectMapper(); //converter para json e vice versa
    private final Map<Usuario, Long> usuariosOnline = new ConcurrentHashMap<>(); //rastreia usuarios online e quando ficou online pela ultima vez


    //construtor
    public UDPServiceImpl(int portaOrigem, int portaDestino, String ipPadrao) throws UnknownHostException {
        this.ipPadrao = ipPadrao;
        this.portaOrigem = portaOrigem;
        this.portaDestino = portaDestino;
        try {
            //cria um socket
            this.dtSocket = new DatagramSocket(this.portaOrigem);

            this.dtSocket.setBroadcast(true);

            System.out.println("UDPServiceImpl estabeleciado na porta: " + this.portaOrigem);
            System.out.println("Broadcast ativado para Sondas.");

            new Thread(new EnviaSonda()).start();
            new Thread(new EscutaSonda()).start();
            new Thread(new VerificaTimeouts()).start();


        } catch (SocketException e) {
            throw new RuntimeException("ERRO ao estabelecer serviço UDP", e);
        }

    }


    //verifica se usuario ainda esta online
    private class VerificaTimeouts implements Runnable {
        // define o timeout em 30 segundos
        private final long TIMEOUT_MS = 30000;

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(5000);

                    long tempoAtual = System.currentTimeMillis();

                    //remove usuario inativo
                    usuariosOnline.entrySet().removeIf(entry -> {
                        Usuario usuario = entry.getKey();
                        Long ultimoAcesso = entry.getValue();

                        // verifica se ultimoacesso foi a 30s atras
                        if (tempoAtual - ultimoAcesso > TIMEOUT_MS) {
                            System.out.println("TIMEOUT: Removendo usuário inativo: " + usuario.getNome());

                            //remove usuario inativo
                            if (usuarioListener!= null) {
                                usuarioListener.usuarioRemovido(usuario);
                            }

                            return true; // Remove esta entrada do ConcurrentHashMap
                        }
                        return false;
                    });

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception e) {
                    System.out.println("error " + e.getMessage());
                }
            }
        }
    }


    //thread de enviar sonda para aparecer nos otros chats
    private class EnviaSonda implements Runnable {
        @SneakyThrows
        @Override
        public void run() {
            while (true) {
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

                    //converte para string e depois para byte
                    String strMensagem = objectMapper.writeValueAsString(mensagem);
                    byte[] bMensagem = strMensagem.getBytes();



                    // O endereço de rede Facul é 192.168.83.
                    String baseIp = ipPadrao;

                    // VAria a ultima casa do endereço de 1 a 255
                    for (int i = 1; i < 255; i++) {
                        InetAddress destino = InetAddress.getByName(baseIp + i);

                        // Cria o pacote usando o ib base
                        DatagramPacket pacote = new DatagramPacket(
                                bMensagem, bMensagem.length,
                                destino,
                                portaDestino // Porta onde os outros clientes estão escutando
                        );

                        // Envia o pacote usando o socket principal da classe
                        dtSocket.send(pacote);
                        System.out.println("SONDA enviada para 254 IPs da sub-rede " + ipPadrao + i + " na porta " + portaDestino);
                    }

                } catch (Exception e) {
                    System.out.println("ERRO ao enviar mensagem de SONDA: " + e.getMessage());
                }
            }
        }
    }


    //thread de escutar receber contato de outras
    private class EscutaSonda implements Runnable {
        @Override
        public void run() {
            while (true) {
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
                    Mensagem msg;
                    try {
                        msg = objectMapper.readValue(jsonRecebido, Mensagem.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    // usuario remetentes (outros chats)
                    Usuario remetente = new Usuario(
                            msg.getUsuario(),
                            Usuario.StatusUsuario.valueOf(msg.getStatus() != null ? msg.getStatus() : "DISPONIVEL"),
                            pacoteRecebido.getAddress()
                    );

                    //ignora mensagens do proprio usuario
                    if (usuario != null && usuario.equals(remetente)) {
                        continue;
                    }

                    //identifica tipo da mensagem e chama o listener
                    switch (msg.getTipoMensagem()) {
                        case sonda: //avisa o listener do usuario que um usuari foi adicionado/atualizado
                            //atualiza o time stamp do acesso
                            usuariosOnline.put(remetente, System.currentTimeMillis());

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
                            System.out.println("fim do chat por: " + remetente.getNome());

                            if (mensagemListener!= null) {
                                mensagemListener.fimChatPelaOutraParte(remetente);
                            }
                            break;
                    }
                } catch (Exception e) {
                    //nao quebra o codigo, continua recebendo mensagens
                    System.out.println("error: " + e.getMessage());
                }
            }
        }
    }


    @Override
    public void enviarMensagem(String mensagem, Usuario destinatario, boolean chatGeral) {
        System.out.println("função enviarMensagem");
        //cria thread para enviar uma mensagem
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

                // Lógica de Endereçamento e Envio
                if (chatGeral) {
                    // formato do ip
                    String baseIp = ipPadrao;
                    int portaFinal = this.portaDestino;

                // for para char a porta e enviar mensagem
                    for (int i = 1; i < 255; i++) {
                        InetAddress destino = InetAddress.getByName(baseIp + i);

                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, destino, portaFinal);
                        dtSocket.send(packet);
                    }
                    System.out.println("mensagem geral enviada para 254 IPs na porta " + portaFinal);

                } else {
                    // mensagem individual enia para ip expecifico
                    InetAddress destino = destinatario.getEndereco(); //ip especifico
                    int portaFinal = this.portaDestino; // porta de escuta padrao

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, destino, portaFinal);
                    dtSocket.send(packet);
                    System.out.println("mensagem enviada para " + destino.getHostAddress() + ":" + portaFinal);
                }

            } catch (Exception e) {
                System.out.println("error: " + e.getMessage());
            }
        }).start();
    }


    @Override
    public void usuarioAlterado(Usuario usuario) {
        this.usuario = usuario;
        System.out.println("função usuarioAlterado");
        //chama sempre que mandar uma sonda
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