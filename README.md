# redes-chatP2P
Desenvolver um aplicativo de troca de mensagens instantâneas (chat) P2P com as seguintes funcionalidades:
- Descoberta de usuários on-line na mesma sub-rede através do algoritmo:
  - Envia datagramas UDP de sonda contendo o seu nome de usuário e status para todos os endereços de host da mesma sub-rede. Quando algum endereço enviar também uma mensagem de sonda, cadastrar ele na lista de usuários on-line, com o seu devido status;
  - Cada cliente de chat ao receber uma mensagem de radar (sonda), deve:
    - cadastrar o usuário recebido na sua lista de usuários on-line, juntamente com o endereço IP, se ele já não existir;
    - atualizar o status do usuário;
    - ignorar mensagens de radar recebidas de si próprio;
  - Os clientes deverão implementar uma função onde são enviadas mensagens de radar periódicos (cada 5s);
  - Sempre que ficar sem receber mensagens de radar de um usuário por tempo superior a 30s, este usuário deve ser retirado da lista de usuários on-line.
- Para qualquer usuário on-line é possível abrir um diálogo de chat, através do envio de mensagens individuais (ver layout abaixo). Mensagens individuais recebidas vão para a janela do IP referente ao usuário que enviou, e caso não exista a janela esta deverá ser criada;
- É possível manter sessões de chat simultâneas com vários usuários através de janelas diferentes;
- Se um dos lados fechar a sua janela, deverá ser dado um aviso ao outro usuário e automaticamente fechada a janela daquela sessão. Este aviso se dará através do envio de uma mensagem do tipo “fim_chat” (ver layout abaixo);
- Sempre existirá uma janela aberta para mensagens do grupo, que seria um chat coletivo de toda a subrede. Para enviar mensagens ao grupo o usuário deverá enviar uma mensagem deste tipo a todos os nós da subrede (ver layout abaixo). Todas as mensagens recebidas deste tipo devem ir para a janela de chat do grupo;
- Para implementação da interface pode ser utilizado o esqueleto em Java-Swing fornecido;
- Todas as mensagens devem ser enviadas pela porta 8080

LAYOUT DAS MENSAGENS: 
- Mensagem de SONDA (apresentação):
  ~~~
    {
      “tipoMensagem”: “sonda”,
      “usuario”: “<nomeusuario>”,
      “status”: “<status>”
    }
  ~~~

- Mensagem de CHAT INDIVIDUAL:
  ~~~
    {
      “tipoMensagem”: “msg_individual”,
      “usuario”: “<nomeusuario>”,
      “status”: “<status>”,
      “msg”: “<mensagem>” 
    }
  ~~~

- Mensagem FIM_CHAT:
  ~~~
    {
      “tipoMensagem”: “fim_chat”,
      “usuario”: “<nomeusuario>”,
      “status”: “<status>”,
      “msg”: “<mensagem>” 
    }
  ~~~

- Mensagem de CHAT GRUPO:
  ~~~
    {
      “tipoMensagem”: “msg_grupo”,
      “usuario”: “<nomeusuario>”,
      “status”: “<status>”,
      “msg”: “<mensagem>” 
    }
  ~~~