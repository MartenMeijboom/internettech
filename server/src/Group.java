import java.util.ArrayList;

public class Group {

    private ClientThread owner;
    private ArrayList<ClientThread> members;

    private String name;

    public Group(ClientThread owner, String name){
        this.owner = owner;
        this.name = name;
        members =new ArrayList<>();
    }

    public void addMember(ClientThread member){
        if(members.contains(member)){
            member.sendToClient("-ERR already a member of this group");
        }else{
            members.add(member);
        }
    }

    public void removeMember(ClientThread member){
        if(members.contains(member)){
            members.remove(member);
        }else {
            member.sendToClient("-ERR not a member");
        }
    }

    public void broadCastMessage(String line){
        owner.sendToClient("BCSTG " + line);
        for (ClientThread t:members) {
            t.sendToClient("BCSTG " + line);
        }
    }

    public String getName(){
        return name;
    }

    public ClientThread getOwner(){
        return owner;
    }

    public int getAmountOfMembers(){
        return members.size();
    }
}
