package br.ufsm.poli.csi.redes.service;
import br.ufsm.poli.csi.redes.model.Usuario;

//define as ações de rede que a interface gráfica ChatClientSwing pode pedir
public interface UDPService {

    /**
     * FEITO
     * Envia uma mensagem para um destinatário
     * @param mensagem
     * @param destinatario
     * @param chatGeral
     */
    void enviarMensagem(String mensagem, Usuario destinatario, boolean chatGeral);

    void usuarioRemovido(Usuario usuario);

    /**
     * FEITO
     * Notifica que o próprio usuário foi alterado
     * @param usuario
     */
    void usuarioAlterado(Usuario usuario);

    /**
     * FEITO
     * Adiciona um listener para indicar o recebimento de mensagens
     * @param listener
     */
    void addListenerMensagem(UDPServiceMensagemListener listener);

    /**
     * FEITO
     * Adiciona um listener para indicar recebimento e/ou alterações em usuários
     * @param listener
     */
    void addListenerUsuario(UDPServiceUsuarioListener listener);

    /**FEITO
     * Método  a ser chamado para encerrar o chat, quando o usuário clica para fechar o chat na interface
     * @param usuario
     */
    void fimChat(Usuario usuario);
}
