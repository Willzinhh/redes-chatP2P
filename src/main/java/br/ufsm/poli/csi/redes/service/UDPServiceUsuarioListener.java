package br.ufsm.poli.csi.redes.service;

import br.ufsm.poli.csi.redes.model.Usuario;

/* avisos sobre usuarios
 * qualquer classe que queira ouvir atualizações de usuarios deve ter os metodos usuarioAdicionado, usuarioRemovido e usuarioAlterado
 * comunicação entre camada de rede e interface gráfica (rede avisa a interface sobre mudanças na lista de usuarios)
 */
public interface UDPServiceUsuarioListener {

    /**
     * Notifica que um usuário foi adicionado
     * @param usuario
     */
    void usuarioAdicionado(Usuario usuario);

    /**
     * Notifica que um usuário foi removido
     * @param usuario
     */
    void usuarioRemovido(Usuario usuario);

    /**
     * Notifica que um usuário foi alterado
     * @param usuario
     */
    void usuarioAlterado(Usuario usuario);

}
