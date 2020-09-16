package com.idemia.tec.jkt.cardiotest.response;

import com.google.gson.Gson;

import java.util.List;

public class RandomQuoteResponse {

    private String _id;
    private List<String> tags;
    private int length;
    private String content;
    private String author;

    public RandomQuoteResponse() {}

    public RandomQuoteResponse(String _id, List<String> tags, int length, String content, String author) {
        this._id = _id;
        this.tags = tags;
        this.length = length;
        this.content = content;
        this.author = author;
    }

    public String get_id() { return _id; }

    public void set_id(String _id) { this._id = _id; }

    public List<String> getTags() { return tags; }

    public void setTags(List<String> tags) { this.tags = tags; }

    public int getLength() { return length; }

    public void setLength(int length) { this.length = length; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }

    public void setAuthor(String author) { this.author = author; }

    // debug
    public String toJson() { return new Gson().toJson(this); }

}
