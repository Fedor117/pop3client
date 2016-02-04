package controller;

import view.MemoFrame;

public class EntryPoint implements Runnable {
    @Override
    public void run() {
        new MemoFrame();
    }

    public static void main(String[] args) {
        new EntryPoint().run();
    }
}
