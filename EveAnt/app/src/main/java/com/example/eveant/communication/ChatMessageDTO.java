package com.example.eveant.communication;

public class ChatMessageDTO {
    private String senderUsername;
    private String recipientUsername;
    private String content;
    private String timestamp;

    public String getSenderUsername(){
        return this.senderUsername;
    }

    public String getRecipientUsername(){
        return this.recipientUsername;
    }
    public String getContent(){
        return this.content;
    }
    public String getTimestamp(){
        return this.timestamp;
    }

    public void setSenderUsername(String senderUsername){
        this.senderUsername=senderUsername;
    }
    public void setRecipientUsername(String recipientUsername){
        this.recipientUsername=recipientUsername;
    }
    public void setContent(String content){
        this.content=content;
    }
    public void setTimestamp(String timestamp){
        this.timestamp=timestamp;
    }


}
