import libs.Message;
import libs.Command;
import libs.IO;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Client implements Runnable {
    IO io;
    String userName;
    Client partner;
    boolean pendingInvite = false;
    Set<String> rejected = new HashSet<>();

    public Client(Socket s, String id) throws IOException {
        this.io = new IO(s, id);
    }

    public void run() {
        try {
            io.handle(new IO.Handler() {
                @Override
                public void onRequest(Message message) throws IOException {
                    Command cmd = message.cmd();
                    System.out.println(cmd);
                    switch (cmd) {
                        case REGISTER -> register(message.data());
                        case ACCEPT -> accept(message.data());
                        case REJECT -> reject(message.data());
                        case LEAVE -> leave();
                        case MESSAGE -> message(message.data());
                        case CLOSE -> close();
                        default -> {
                            io.send(new Message(Command.ERROR, "Invalid Command"));
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void register(String data) throws IOException {
        System.out.println("Register user: " + data);
        this.userName = data;
        if (Server.clients.containsKey(data)) {
            this.io.send(new Message(Command.USERNAME_EXISTED));
        } else {
            Server.clients.put(this.userName, this);
            findPartner();
        }
    }

    public void findPartner() throws IOException {
        for (String userName : Server.waitingList) {
            Client client = Server.clients.get(userName);
            boolean notSelf = !client.userName.equals(this.userName);
            boolean notPaired = client.partner == null;
            boolean notRejected = !this.rejected.contains(client.userName);
            if (notSelf && notPaired && notRejected) {
                this.invite(client);
                break;
            }
        }
        if (!pendingInvite && partner == null) {
            addToWaiting();
        }
    }

    public void addToWaiting() throws IOException {
        io.send(new Message(Command.WAITING));
        Server.waitingList.add(userName);
    }

    public void invite(Client target) throws IOException {
        System.out.println(this.userName + " invite " + target.userName);
        this.io.send(new Message(Command.INVITE, target.userName));
        this.pendingInvite = true;
    }

    public void reject(String userName) throws IOException {
        this.rejected.add(userName);
        this.pendingInvite = false;
        findPartner();
    }

    public void accept(String userName) throws IOException {
        Client target = Server.clients.get(userName);
        this.pendingInvite = false;

        if (target == null || target.partner != null) {
            this.io.send(new Message(Command.PAIR_FAIL));
            findPartner();
        } else {
            this.partner = target;
            this.partner.partner = this;

            this.io.send(new Message(Command.PAIRED, userName));
            this.partner.io.send(new Message(Command.PAIRED, this.userName));

            Server.waitingList.remove(this.userName);
            Server.waitingList.remove(userName);
        }
    }

    public void message(String data) throws IOException {
        this.partner.io.send(new Message(Command.MESSAGE, data));
    }

    public void unPair() throws IOException {
        this.partner = null;
        this.io.send(new Message(Command.UNPAIRED));
        findPartner();
    }

    public void leave() throws IOException {
        if(this.partner != null)
            this.partner.unPair();
        this.unPair();
    }

    public void close(){
        Server.waitingList.remove(this.userName);
        Server.clients.remove(this.userName);
    }
}