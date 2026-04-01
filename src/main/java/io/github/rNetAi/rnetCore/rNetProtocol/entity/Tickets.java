package io.github.rNetAi.rnetCore.rNetProtocol.entity;

import java.util.List;
import java.util.Map;

public class Tickets {
    private final Map<Long , List<String>> tickets;

    public Tickets(Map<Long , List<String>> tickets){
        this.tickets = tickets;
    }

    public int getSize(){
        return tickets.size();
    }

    public List<String> getResourceTicket(long id){
        return tickets.getOrDefault(id , null);
    }
}
