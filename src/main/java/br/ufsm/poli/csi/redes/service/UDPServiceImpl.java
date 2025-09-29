package br.ufsm.poli.csi.redes.service;

import br.ufsm.poli.csi.redes.model.Usuario;

/*
* criar DatagramSocket
* montar DatagramPacket
* chamar socket.send()
* receive()
* broadcast para descobrir usuarios
* */
public class UDPServiceImpl implements UDPService {
    @Override
    public void enviarMensagem(String mensagem, Usuario destinatario, boolean chatGeral) {

    }

    @Override
    public void usuarioAlterado(Usuario usuario) {

    }

    @Override
    public void addListenerMensagem(UDPServiceMensagemListener listener) {

    }

    @Override
    public void addListenerUsuario(UDPServiceUsuarioListener listener) {

    }
}
