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
    }

    public void addMember(ClientThread member){
        members.add(member);
    }

    public void removeMember(ClientThread member){
        members.remove(member);
    }

    public void broadCastMessage(String line){
        for (ClientThread t:members) {
            t.writeToClient("BCSTG " + line);
        }
    }

    public String getName(){
        return name;
    }

    public int getAmountOfMembers(){
        return members.size();
    }
}
