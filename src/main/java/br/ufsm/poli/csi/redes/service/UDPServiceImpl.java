//package br.ufsm.poli.csi.redes.service;
//
//import br.ufsm.poli.csi.redes.model.Mensagem;
//import br.ufsm.poli.csi.redes.model.Usuario;
//import br.ufsm.poli.csi.redes.swing.ChatClientSwing;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.SneakyThrows;
//import netscape.javascript.JSObject;
//import org.w3c.dom.ls.LSOutput;
//
//import javax.xml.crypto.Data;
//import java.io.IOException;
//import java.net.*;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///*
// * criar DatagramSocket
// * montar DatagramPacket
// * chamar socket.send()
// * receive()
// * broadcast para descobrir usuarios
// * mensagem de sonda afeta usuario listener - tem lista de usuarios na classe
// * */
//
////mensagem na interface --> chama essa classe
//public class UDPServiceImpl implements UDPService {
//    //atributos
//    private DatagramSocket dtSocket; //socket principal para a rede
////    private final int porta = 8080;
//    private int portaOrigem; //escuta
//    private int portaDestino; //envia mensagem
//    private Usuario usuario = null; //referencia o usuario atual
//    private UDPServiceMensagemListener mensagemListener = null; //avisar sobre novas mensagens
//    private UDPServiceUsuarioListener usuarioListener = null; //avisar sobre novos usuarios
//    private final ObjectMapper objectMapper = new ObjectMapper(); //converter para json e vice versa
//    private final Map<Usuario, Long> usuariosOnline = new ConcurrentHashMap<>(); //rastreia usuarios online e quando ficou online pela ultima vez
//
//
////    //construtor
////    public UDPServiceImpl(int portaOrigem, int portaDestino) {
////        this.portaOrigem = portaOrigem;
////        this.portaDestino = portaDestino;
////        try {
////            //cria um socket
////            this.dtSocket = new DatagramSocket(this.portaOrigem);
////            System.out.println("UDPServiceImpl estabeleciado na porta: " + this.portaOrigem);
////
////            //threads
////            new Thread(new EnviaSonda()).start();
////            new Thread(new EscutaSonda()).start();
////            //timeout
//////            new Thread(new VerificaTimeouts()).start();
////
////        } catch (SocketException e) {
////            throw new RuntimeException("ERRO ao estabelecer serviço UDP", e);
////        }
////
////    }
//
//
//
//    // CONSTRUTOR CORRIGIDO
//    public UDPServiceImpl(int portaOrigem, int portaDestino) {
//        this.portaOrigem = portaOrigem;
//        this.portaDestino = portaDestino;
//        try {
//            //cria um socket
//            this.dtSocket = new DatagramSocket(this.portaOrigem);
//
//            // **[CORREÇÃO 1]**: Habilitar o broadcast no socket
//            this.dtSocket.setBroadcast(true);
//
//            System.out.println("UDPServiceImpl estabeleciado na porta: " + this.portaOrigem);
//            System.out.println("Broadcast ativado para Sondas.");
//
//            //threads
//            new Thread(new EnviaSonda()).start();
//            new Thread(new EscutaSonda()).start();
//            //...
//        } catch (SocketException e) {
//            throw new RuntimeException("ERRO ao estabelecer serviço UDP", e);
//        }
//    }
//
//
//    //thread de enviar sonda (para mostrar usuario na lista dos outros) - atualizada moodle
//    private class EnviaSonda implements Runnable {
//        @SneakyThrows
//        @Override
//        public void run() {
////            System.out.println("entrou no run da classe EnviaSonda");
//            while (true) {
////                System.out.println("entrou no while true do run da classe EnviaSonda");
//                Thread.sleep(5000);
//                if (usuario == null) {
//                    System.out.println("usuario == null");
//                    continue;
//                }
//
//                try {
//                    //manda sonda
//                    Mensagem mensagem = new Mensagem();
//                    mensagem.setTipoMensagem(Mensagem.TipoMensagem.sonda);
//                    mensagem.setUsuario(usuario.getNome());
//                    mensagem.setStatus(usuario.getStatus().toString());
//
//                    //converte para string
//                    ObjectMapper mapper = new ObjectMapper();
//
//                    //converte para json
//                    String strMensagem = mapper.writeValueAsString(mensagem);
//
//                    //converte para byte
//                    byte[] bMensagem = strMensagem.getBytes();
//
//                    //envias para rtodos os IPs da sub rede
//                    InetAddress broadcast = InetAddress.getByName("255.255.255.255");
////                    for (int i = 1; i < 255; i++) {
////                        System.out.println("entoru no for ENVIA\n");
//                        DatagramPacket pacote = new DatagramPacket(
//                                bMensagem, bMensagem.length,
////                                InetAddress.getByName("192.168.83." + i),
//                                broadcast,
//                                portaDestino //da outra janela
//                        );
//
//                        //envia o socket principal da classe
//                        dtSocket.send(pacote);
////                    }
//                    System.out.println("SONDA enviada para sub rede na porta " + portaDestino);
//
//                } catch (Exception e) {
//                    System.out.println("ERRO ao enviar mensagem de SONDA: " + e.getMessage());
//                }
//
////                System.out.println("FIM do while true ENVIA sonda");
//            }
//        }
//    }
//
//
//    //thread de escutar, espera algo chegar na porta de origem
//    private class EscutaSonda implements Runnable {
//        @Override
//        public void run() {
////            System.out.println("entrou no run da classe ESCUTA Sonda");
//
//            while (true) {
////                System.out.println("entrou no while true do ESCUTA");
//
//                try {
//                    //buffer vazio para receber dados da rede
//                    byte[] buffer = new byte[4096]; //buffer para json
//                    DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);
//
//                    //espera pacote chegar na porta de escuta
//                    try {
//                        dtSocket.receive(pacoteRecebido);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//
//                    //converte bytes para string json
//                    String jsonRecebido = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength()); //pega apenas o conteudo da mensagem
//
//                    //converte json para mensagem de volta
//                    ObjectMapper mapper = new ObjectMapper();
//                    Mensagem msg;
//                    try {
//                        msg = mapper.readValue(jsonRecebido, Mensagem.class);
//                    } catch (JsonProcessingException e) {
//                        throw new RuntimeException(e);
//                    }
//
//                    // objeto Usuario (remetente da msg) - nome e status da msg, endereço IP do pacote
//                    Usuario remetente = new Usuario(
//                            msg.getUsuario(),
//                            Usuario.StatusUsuario.valueOf(msg.getStatus() != null ? msg.getStatus() : "DISPONIVEL"),
//                            pacoteRecebido.getAddress()
//                    );
//
//                    //ignora mensagens do proprio usuario
//                    if (usuario != null && usuario.equals(remetente)) {
//                        continue; //pula para proxima iteraçao do loop (ignora)
//                    }
//
//                    //identifica tipo da mensagem e chama o listener
//                    switch (msg.getTipoMensagem()) {
//                        case sonda: //avisa o listener do usuario que um usuari foi adicionado/atualizado
//                            if (usuarioListener != null) {
//                                usuarioListener.usuarioAdicionado(remetente);
//                            }
//                            break;
//
//                        case msg_individual: //avsa o listener de mensagem
//                            if (mensagemListener != null) {
//                                mensagemListener.mensagemRecebida(msg.getMsg(), remetente, false);
//                            }
//                            break;
//
//                        case msg_grupo: //avsa o listener de mensagem tb
//                            if (mensagemListener != null) {
//                                mensagemListener.mensagemRecebida(msg.getMsg(), remetente, true);
//                            }
//                            break;
//
//                        case fim_chat:
//                            System.out.println("FIM DE CHAT: " + remetente.getNome());
//                            break;
//
//                    }
//                } catch (Exception e) {
//                    //nao quebra o codigo, continua recebendo mensagens
//                    System.out.println("ERRO na thread de escuta: " + e.getMessage());
//                }
//
//
//            }
//        }
//    }
//
//
////    @Override
////    public void enviarMensagem(String mensagem, Usuario destinatario, boolean chatGeral) {
////        System.out.println("função enviarMensagem");
////        //cria thread para enviar uma mensagem = manda um pacote e termina
////        new Thread(() -> {
////            try {
////                                        // se chatGeral = true o tipo é msg_grupo; se = false é msg_individual
////                Mensagem.TipoMensagem tipo = chatGeral ? Mensagem.TipoMensagem.msg_grupo : Mensagem.TipoMensagem.msg_individual;
////                //monta objetp do tipo Mensagem
////                Mensagem objMsg = Mensagem.builder().
////                        tipoMensagem(tipo).
////                        usuario(this.usuario.getNome()).
////                        status(this.usuario.getStatus().toString()).
////                        msg(mensagem).build();
////
////                //para json
////                String jsonMsg = objectMapper.writeValueAsString(objMsg);
////                byte[] buffer = jsonMsg.getBytes();
////
////                //endereço de destino
////                InetAddress destino;
////                int portaFinal;
////                if (chatGeral) { //se for chat_geral manda para endereço broadcast
////                    destino = InetAddress.getByName("255.255.255.255"); //broadcast
////                    portaFinal = this.portaDestino; //manda para porta do outro usuario
////                } else {
////                    destino = destinatario.getEndereco(); //IP especifico
////                    portaFinal = destinatario.getEndereco().equals(this.usuario.getEndereco()) ? this.portaDestino : this.portaOrigem;
////                }
////
////                //cria pacote
////                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, destino, portaFinal);
////                //envia pacote
////                dtSocket.send(packet);
////                System.out.println("Mensagem enviada para " + destino.getHostAddress() + ":" + portaFinal);
////            } catch (Exception e) {
////                System.out.println("ERRO ao enviar mensagem: " + e.getMessage());
////            }
////        }).start();
////    }
//
//
//
//
//    @Override
//    public void usuarioAlterado(Usuario usuario) {
//        this.usuario = usuario;
//        System.out.println("função usuarioAlterado");
//        //chama sempre que mandar uma osnda
//    }
//
//    @Override
//    public void addListenerMensagem(UDPServiceMensagemListener listener) {
//        //"esse eh o meu listener, quando receber mensagem me chama por aqui"
//        //listeners para notificar a interface
//        this.mensagemListener = listener;
//        System.out.println("função addListenerMensagem");
//    }
//
//    @Override
//    public void addListenerUsuario(UDPServiceUsuarioListener listener) {
//        this.usuarioListener = listener;
//        System.out.println("função addListenerUsuario");
//    }
//}
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
    private DatagramSocket dtSocket; //socket principal para a rede
    private int portaOrigem; //escuta
    private int portaDestino; //envia mensagem (porta de escuta padrão para todos os pares)
    private Usuario usuario = null; //referencia o usuario atual
    private UDPServiceMensagemListener mensagemListener = null; //avisar sobre novas mensagens
    private UDPServiceUsuarioListener usuarioListener = null; //avisar sobre novos usuarios
    private final ObjectMapper objectMapper = new ObjectMapper(); //converter para json e vice versa
    private final Map<Usuario, Long> usuariosOnline = new ConcurrentHashMap<>(); //rastreia usuarios online e quando ficou online pela ultima vez


    //construtor
    public UDPServiceImpl(int portaOrigem, int portaDestino, String ipPadrao) throws UnknownHostException {
        this.ipPadrao = ipPadrao;
        this.portaOrigem = portaOrigem;
        this.portaDestino = portaDestino;
        try {
            //cria um socket
            this.dtSocket = new DatagramSocket(this.portaOrigem);

            // **[CORREÇÃO 1]**: Habilitar o broadcast no socket.
            // Essencial para o envio de Sonda por 255.255.255.255.
            this.dtSocket.setBroadcast(true);

            System.out.println("UDPServiceImpl estabeleciado na porta: " + this.portaOrigem);
            System.out.println("Broadcast ativado para Sondas.");

            //threads
            new Thread(new EnviaSonda()).start();
            new Thread(new EscutaSonda()).start();
            //timeout
            // new Thread(new VerificaTimeouts()).start();

        } catch (SocketException e) {
            throw new RuntimeException("ERRO ao estabelecer serviço UDP", e);
        }

    }


    //thread de enviar sonda (para mostrar usuario na lista dos outros) - atualizada moodle
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
                    // O ObjectMapper já está definido como atributo da classe principal (this.objectMapper)
                    String strMensagem = objectMapper.writeValueAsString(mensagem);
                    byte[] bMensagem = strMensagem.getBytes();

                    // **[NOVA LÓGICA DE ENVIO - VARREDURA DA SUB-REDE 192.168.83.x]**

                    // O endereço de rede base é 192.168.83.
                    String baseIp = ipPadrao;

                    // Envia um pacote para cada endereço IP na faixa 192.168.83.1 até 192.168.83.254
                    // Note que este método de "scanning" é menos eficiente que o broadcast, mas
                    // atende ao seu requisito de IP específico.
                    for (int i = 1; i < 255; i++) {
                        InetAddress destino = InetAddress.getByName(baseIp + i);

                        // Cria o pacote usando o IP específico
                        DatagramPacket pacote = new DatagramPacket(
                                bMensagem, bMensagem.length,
                                destino, // IP específico, ex: 192.168.83.10
                                portaDestino // Porta onde os outros clientes estão escutando
                        );

                        // Envia o pacote usando o socket principal da classe
                        dtSocket.send(pacote);
                        System.out.println("SONDA enviada para 254 IPs da sub-rede " + ipPadrao + i + " na porta " + portaDestino);
                    }


                } catch (Exception e) {
                    // Incluindo UnknownHostException
                    System.out.println("ERRO ao enviar mensagem de SONDA: " + e.getMessage());
                }
            }
        }
    }


    //thread de escutar, espera algo chegar na porta de origem
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


//    @Override
//    public void enviarMensagem(String mensagem, Usuario destinatario, boolean chatGeral) {
//        System.out.println("função enviarMensagem");
//        //cria thread para enviar uma mensagem = manda um pacote e termina
//        new Thread(() -> {
//            try {
//                // se chatGeral = true o tipo é msg_grupo; se = false é msg_individual
//                Mensagem.TipoMensagem tipo = chatGeral ? Mensagem.TipoMensagem.msg_grupo : Mensagem.TipoMensagem.msg_individual;
//                //monta objetp do tipo Mensagem
//                Mensagem objMsg = Mensagem.builder().
//                        tipoMensagem(tipo).
//                        usuario(this.usuario.getNome()).
//                        status(this.usuario.getStatus().toString()).
//                        msg(mensagem).build();
//
//                //para json
//                String jsonMsg = objectMapper.writeValueAsString(objMsg);
//                byte[] buffer = jsonMsg.getBytes();
//
//                //endereço de destino
//                InetAddress destino;
//                int portaFinal;
//                if (chatGeral) { //se for chat_geral manda para endereço broadcast
//                    destino = InetAddress.getByName("255.255.255.255"); //broadcast
//                    portaFinal = this.portaDestino; //manda para porta do outro usuario
//                } else {
//                    destino = destinatario.getEndereco(); //IP especifico
//
//                    // **[CORREÇÃO 2]**: A porta final deve ser a porta de escuta do destinatário,
//                    // que é a portaDestino padrão do aplicativo.
//                    portaFinal = this.portaDestino;
//                }
//
//                //cria pacote
//                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, destino, portaFinal);
//                //envia pacote
//                dtSocket.send(packet);
//                System.out.println("Mensagem enviada para " + destino.getHostAddress() + ":" + portaFinal);
//            } catch (Exception e) {
//                System.out.println("ERRO ao enviar mensagem: " + e.getMessage());
//            }
//        }).start();
//    }
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

            // Lógica de Endereçamento e Envio
            if (chatGeral) {
                // **[NOVO BROADCAST/VARREDURA para CHAT GERAL]**: Envia para 192.168.83.x
                String baseIp = ipPadrao;
                int portaFinal = this.portaDestino;

                for (int i = 1; i < 255; i++) {
                    InetAddress destino = InetAddress.getByName(baseIp + i);

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, destino, portaFinal);
                    dtSocket.send(packet);
                    // System.out.println("Mensagem de GRUPO enviada para " + destino.getHostAddress() + ":" + portaFinal);
                }
                System.out.println("Mensagem de GRUPO enviada para 254 IPs na porta " + portaFinal);

            } else {
                // **[MENSAGEM INDIVIDUAL]**: Envia para IP específico
                InetAddress destino = destinatario.getEndereco(); //IP especifico
                int portaFinal = this.portaDestino; // Porta de escuta padrão

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, destino, portaFinal);
                dtSocket.send(packet);
                System.out.println("Mensagem INDIVIDUAL enviada para " + destino.getHostAddress() + ":" + portaFinal);
            }

            // **[CORREÇÃO DE EXIBIÇÃO]**: Exibe a mensagem na própria interface APÓS o envio.
            if (mensagemListener != null) {
                mensagemListener.mensagemRecebida(mensagem, this.usuario, chatGeral);
            }

        } catch (Exception e) {
            System.out.println("ERRO ao enviar mensagem: " + e.getMessage());
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