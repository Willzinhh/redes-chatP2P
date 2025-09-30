package br.ufsm.poli.csi.redes.swing;

import br.ufsm.poli.csi.redes.model.Usuario;
import br.ufsm.poli.csi.redes.service.UDPService;
import br.ufsm.poli.csi.redes.service.UDPServiceImpl;
import br.ufsm.poli.csi.redes.service.UDPServiceMensagemListener;
import br.ufsm.poli.csi.redes.service.UDPServiceUsuarioListener;
import lombok.Getter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

//nao mexer em nada

/**
 * 
 * User: Rafael
 * Date: 13/10/14
 * Time: 10:28
 * 
 */

/* interface grafica
* implementa as interfaces nas classes internas - MensagemListener e UsuarioListener
* recebe avisos sobre mensagens e usuarios da camada de rede
*
* JPANEL
* biblioteca de interface grafica - JFrame é a janela principal, com botões de fechar e minimizar
* JPanel vai dentro do JFrame, é um container para organizar outros componentes (botões, campos de texto, listas)
* LayoutManager organiza os componentes no espaço pelo GridBagLayout
* add(componente, constraints) = "pinte esse componente nessa tela usando essas regras de posicionamento"
* */
public class ChatClientSwing extends JFrame { //JFrame - classe base para janelas no Java Swing

    private Usuario meuUsuario; //infos do usuario local
    private JList listaChat; //componente visual - lista de usuarios
    private DefaultListModel<Usuario> dfListModel; //onde os nomes são adicionados/removidos, por trás da lista
    private JTabbedPane tabbedPane = new JTabbedPane(); //painel com abas para cada conversa
    private Set<Usuario> chatsAbertos = new HashSet<>(); //conjunto - controla quais chats privados já estão abertos (evitar duplicados)
    private UDPService udpService = new UDPServiceImpl(); // por aqui a interface pede ações de rede
    private Usuario USER_GERAL = new Usuario("Geral", null, null); //aba de chat geral (objeto especial)

    public ChatClientSwing() throws UnknownHostException { //construtor - constroi a janela
        setLayout(new GridBagLayout()); //define o layout "planilha"
        JMenuBar menuBar = new JMenuBar(); //cria a barra de menu no topo da janela
        JMenu menu = new JMenu("Status"); //cria o menu chamado Status

        // ------- Criação do menu Status -------
        ButtonGroup group = new ButtonGroup(); //apenas 1 estado pode ser selecionado por vez

        //status DISPONIVEL
        JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(Usuario.StatusUsuario.DISPONIVEL.name()); //cria um item de menu selecionavel "disponivel"
        rbMenuItem.setSelected(true); //define DISPONIVEL como PADRÃO
        rbMenuItem.addActionListener(new ActionListener() { //adiciona um ActionListener (tipo um espião de cliques)
            @Override
            public void actionPerformed(ActionEvent actionEvent) { //roda quando o usuário clica no item DISPONIVEL
                ChatClientSwing.this.meuUsuario.setStatus(Usuario.StatusUsuario.DISPONIVEL); //muda o status do objeto meuUsuario
                udpService.usuarioAlterado(meuUsuario); //chama o metodo usuarioAlterado para a rede anunciar a mudança
            }
        });
        group.add(rbMenuItem); //adiciona ao grupo, para garantir seleção unica
        menu.add(rbMenuItem); //adiciona item ao menu Status

        //status NAO_PERTURBE
        rbMenuItem = new JRadioButtonMenuItem(Usuario.StatusUsuario.NAO_PERTURBE.name()); //cria um item de menu selecionavel "nao_perturbe"
        rbMenuItem.addActionListener(new ActionListener() { //adiciona um ActionListener (tipo um espião de cliques)
            @Override
            public void actionPerformed(ActionEvent actionEvent) { //roda quando o usuário clica no item NAO_PERTURBE
                ChatClientSwing.this.meuUsuario.setStatus(Usuario.StatusUsuario.NAO_PERTURBE); //muda o status do objeto meuUsuario
                udpService.usuarioAlterado(meuUsuario); //chama o metodo usuarioAlterado para a rede anunciar a mudança
            }
        });
        group.add(rbMenuItem); //adiciona ao grupo, para garantir seleção unica
        menu.add(rbMenuItem); //adiciona item ao menu Status

        //status VOLTO_LOGO
        rbMenuItem = new JRadioButtonMenuItem(Usuario.StatusUsuario.VOLTO_LOGO.name()); //cria um item de menu selecionavel "volto_logo"
        rbMenuItem.addActionListener(new ActionListener() { //adiciona um ActionListener (tipo um espião de cliques)
            @Override
            public void actionPerformed(ActionEvent actionEvent) { //roda quando o usuário clica no item VOLTO_LOGO
                ChatClientSwing.this.meuUsuario.setStatus(Usuario.StatusUsuario.VOLTO_LOGO); //muda o status do objeto meuUsuario
                udpService.usuarioAlterado(meuUsuario); //chama o metodo usuarioAlterado para a rede anunciar a mudança
            }
        });
        group.add(rbMenuItem); //adiciona ao grupo, para garantir seleção unica
        menu.add(rbMenuItem); //adiciona item ao menu Status


        menuBar.add(menu);
        this.setJMenuBar(menuBar); //instala a barra de menu na janela


        // ------- Fechar abas com o otão direito do mouse -------
        tabbedPane.addMouseListener(new MouseAdapter() { //adiciona um MouseListener (tipo um espião de cliques so que do mouse)
            @Override
            public void mousePressed(MouseEvent e) { //roda quando o mouse é pressionado sobre as abas
                super.mousePressed(e);
                if (e.getButton() == MouseEvent.BUTTON3) { //se for o botão direito do mouse
                    // --- cria um menu pop-up com a opção Fechar ---
                    JPopupMenu popupMenu =  new JPopupMenu();
                    final int tab = tabbedPane.getUI().tabForCoordinate(tabbedPane, e.getX(), e.getY());
                    JMenuItem item = new JMenuItem("Fechar");
                    item.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            PainelChatPVT painel = (PainelChatPVT) tabbedPane.getTabComponentAt(tab);
                            tabbedPane.remove(tab);
                            chatsAbertos.remove(painel.getUsuario());
                        }
                    });
                    popupMenu.add(item);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });


        // ------- Montagem da janela, tipo um CSS -------
        //adiciona a lista de usuarios a esquerda e as abas de chat a direita na janela, pelo GridBagLayout
        add(new JScrollPane(criaLista()), new GridBagConstraints(0, 0, 1, 1, 0.1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        add(tabbedPane, new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        //define o tamanho da janela
        setSize(800, 600);

        //centraliza a janela na tela
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int x = (screenSize.width - this.getWidth()) / 2;
        final int y = (screenSize.height - this.getHeight()) / 2;
        this.setLocation(x, y);


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //programa fecha quando clica no X
        setTitle("Chat P2P - Redes de Computadores"); //titulo da janela


        // ------- Inicialização do chat -------
        String nomeUsuario = JOptionPane.showInputDialog(this, "Digite seu nome de usuario: "); //pede o nome do usuário
        this.meuUsuario = new Usuario(nomeUsuario, Usuario.StatusUsuario.DISPONIVEL, InetAddress.getLocalHost()); //cria objeto meuUsuario com nome, status padrão e IP local
        udpService.usuarioAlterado(meuUsuario); //anuncia a propria presença na rede

        //conexão das camadas
        udpService.addListenerMensagem(new MensagemListener()); //entrega o 'comunicador de mensagens' para a camada de serviço
        udpService.addListenerUsuario(new UsuarioListener()); //entrega o 'comunicador de usuarios' para a camada de serviço
        setVisible(true); //deixa a janela visivel
    }


    private JComponent criaLista() {
        dfListModel = new DefaultListModel();
        //dfListModel.addElement(new Usuario("Fulano", Usuario.StatusUsuario.NAO_PERTURBE, null));
        //dfListModel.addElement(new Usuario("Cicrano", Usuario.StatusUsuario.DISPONIVEL, null));
        listaChat = new JList(dfListModel);
        listaChat.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    Usuario user = (Usuario) list.getModel().getElementAt(index);
                    if (chatsAbertos.add(user)) {
                        tabbedPane.add(user.toString(), new PainelChatPVT(user, false));
                    }
                }
            }
        });
        chatsAbertos.add(USER_GERAL);
        tabbedPane.add("Geral", new PainelChatPVT(USER_GERAL, true));
        return listaChat;
    }


    // ----- classes internas -----
    @Getter
    class PainelChatPVT extends JPanel { //molde para cada aba de chat

        JTextArea areaChat;
        JTextField campoEntrada;
        Usuario usuario;
        boolean chatGeral = false;

        PainelChatPVT(Usuario usuario, boolean chatGeral) { //construtor - monta a aparencia de uma aba
            setLayout(new GridBagLayout());
            areaChat = new JTextArea(); //area de texto
            this.usuario = usuario;
            areaChat.setEditable(false); //nao editavel
            campoEntrada = new JTextField(); //campo de texto
            this.chatGeral = chatGeral;
            campoEntrada.addActionListener(new ActionListener() { //"espião" (Listener) do campo de texto
                @Override
                public void actionPerformed(ActionEvent e) { //roda quando o usuario digita uma mensagem e aperta Enter
                    ((JTextField) e.getSource()).setText(""); //limpa o campo de texto
                    areaChat.append(meuUsuario.getNome() + "> " + e.getActionCommand() + "\n"); //adiciona a propria mensagem na propria tela
                    udpService.enviarMensagem(e.getActionCommand(), usuario, chatGeral); //pede para a camada de rede enviar a mensagem ao destinatario
                    //fluxo de dados de saída
                }
            });
            add(new JScrollPane(areaChat), new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            add(campoEntrada, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        }

    }

    //implementação do "comunicador" de usuarios
    private class UsuarioListener implements UDPServiceUsuarioListener {
        /*
        os metodos nao sao chamados pela interface grafica e sim pela camada de serviço quando ela recebe um pacote UDP de status de outro usuario
        os metodos manipulam o dfListModel para adicionar, remover ou atualizar um usuario na lista na tela
         */
        @Override
        public void usuarioAdicionado(Usuario usuario) {
            dfListModel.removeElement(usuario);
            dfListModel.addElement(usuario);
        }

        @Override
        public void usuarioRemovido(Usuario usuario) {
            dfListModel.removeElement(usuario);
        }

        @Override
        public void usuarioAlterado(Usuario usuario) {
            dfListModel.removeElement(usuario);
            dfListModel.addElement(usuario);
        }
    }

    //implementação do "comunicador" de mensagens
    private class MensagemListener implements UDPServiceMensagemListener {

        @Override
        public void mensagemRecebida(String mensagem, Usuario remetente, boolean chatGeral) {
            /*
            metodo chamado pela camada de serviço quando um pacote UDP com uma mebnsagem de chat chega
            fluxo de dados de entrada
            encontra a aba de chat correta (geral ou privada) e chama painel.getAreaChat().append() para exibir a mensagem recebida na tela
             */
            PainelChatPVT painel = null;
            if (chatGeral) {
                painel = (PainelChatPVT) tabbedPane.getComponentAt(0);
            } else {
                for (int i = 1; i < tabbedPane.getTabCount(); i++) {
                    PainelChatPVT p = (PainelChatPVT) tabbedPane.getComponentAt(i);
                    if (p.getUsuario().equals(remetente)) {
                        painel = p;
                        break;
                    }
                }
            }
            if (painel != null) {
                painel.getAreaChat().append(remetente.getNome() + "> " + mensagem + "\n");
            }
        }
    }





}
