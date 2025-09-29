package br.ufsm.poli.csi.redes.service;

import br.ufsm.poli.csi.redes.model.Usuario;

/* avisos sobre mensagens
 * qualquer classe que queira ouvir mensagens deve ter o metodo mensagemRecebida
 * comunicação entre camada de rede e interface gráfica (rede avisa a interface que recebeu uma mensgaem)
 */
public interface UDPServiceMensagemListener {

    /**
     * Notifica que uma mensagem foi recebida
     * @param mensagem
     * @param remetente
     * @param chatGeral
     */
    void mensagemRecebida(String mensagem, Usuario remetente, boolean chatGeral);

}
