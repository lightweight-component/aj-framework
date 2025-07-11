package com.ajaxjs.business.utils.scz;

public class Producer implements Runnable {
    private final Resource resource;

    public Producer(Resource resource) {
        this.resource = resource;
    }

    @Override
    public void run() {
        while (true) {
            resource.put();
        }
    }
}