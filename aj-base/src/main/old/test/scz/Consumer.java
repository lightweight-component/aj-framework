package com.ajaxjs.base.scz;

public class Consumer implements Runnable {
    private final Resource resource;

    public Consumer(Resource resource) {
        this.resource = resource;
    }

    @Override
    public void run() {
        while (true){
            resource.remove();
        }
    }
}