package br.ufsm.poli.csi.redes.service;

import br.ufsm.poli.csi.redes.model.Mensagem;
import br.ufsm.poli.csi.redes.model.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.crypto.Data;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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

    //thread de enviar sonda
    private class EnviaSonda implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (usuario == null) {
                    continue;
                }
                //manda sonda
                Mensagem mensagem = new Mensagem();
                mensagem.setTipoMensagem(Mensagem.TipoMensagem.sonda);
                mensagem.setUsuario(usuario.getNome());
                mensagem.setStatus(usuario.getStatus().toString());
                //converte para string
                ObjectMapper mapper = new ObjectMapper();
                String strMensagem = mapper.writeValueAsString(mensagem);
                //converte para byte
                byte[] bMensagem = strMensagem.getBytes();

                //manda packet
                for (int i = 1; i < 255; i++) {
                    DatagramPacket pacote = new DatagramPacket(
                            bMensagem, bMensagem.length,
                            InetAddress.getByName("192.168.83." + i),
                            8080
                    );
                    DatagramSocket socket = new DatagramSocket();
                    socket.send(pacote);
                }
            }
        }
    }
//    private DatagramSocket serverSocket;
//    private final int porta = 8080;

    public UDPServiceImpl() {
//        try {
//            this.serverSocket = new DatagramSocket(porta);
//            serverSocket.setBroadcast(true);
////            serverSocket.setSoTimeout(5000);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void enviarMensagem(String mensagem, Usuario destinatario, boolean chatGeral) {

    }

    private Usuario usuario = null; //referencia o usuario atual
    @Override
    public void usuarioAlterado(Usuario usuario) {
        this.usuario = usuario;
        //chama sempre que mandar uma osnda
    }

    private UDPServiceMensagemListener mensagemListener = null;
    @Override
    public void addListenerMensagem(UDPServiceMensagemListener listener) {
        //"esse eh o meu listener, quando receber mensagem me chama por aqui"
        this.mensagemListener = listener;
    }

    private UDPServiceUsuarioListener usuarioListener = null;
    @Override
    public void addListenerUsuario(UDPServiceUsuarioListener listener) {
        this.usuarioListener = listener;
    }
}
