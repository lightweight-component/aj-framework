package com.ajaxjs.base.scz;

public class Producer implements Runnable {
    private final Resource resource;

    public Producer(Resource resource){
        this.resource=resource;
    }

    @Override
    public void run() {
        while (true){
            resource.put();
        }
    }
}