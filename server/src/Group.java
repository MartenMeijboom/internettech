import java.util.ArrayList;

public class Group {

    private ClientThread owner;
    private ArrayList<ClientThread> members;

    private int id;
    private static int idList = 0;
    private String name;

    public Group(ClientThread owner, String name){
        this.owner = owner;
        this.id = idList++;
        this.name = name;
        members =new ArrayList<>();
    }

    public void addMember(ClientThread member){
        if(members.contains(member)){
            member.writeToClient("-ERR already a member of this group");
        }else{
            members.add(member);
        }
    }

    public void removeMember(ClientThread member){
        if(members.contains(member)){
            members.remove(member);
        }else {
            member.writeToClient("-ERR not a member");
        }
    }

    public void broadCastMessage(String line){
        owner.writeToClient("BCSTG " + line);
        for (ClientThread t:members) {
            t.writeToClient("BCSTG " + line);
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
