package com.lichenglin.gulimall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(10);
        /**
         * completaleFuture
         * runAsync ： 没有返回值；
         * supplyAsync ：有返回值；
         *
         * whenCompleteAsync: 在任务完成后，进行回调函数的调用，回调函数依然使用异步的方式；
         * whenComplete： 在任务完成后，进行回调函数的调用，回调函数使用同一线程；
         *
         * handle:方法执行完成后，再次进行处理（无论方法执行成功或失败）
         *
         * 线程串行化：将多个线程任务，进行执行顺序的编排；
         * thenRun/thenRunAsync: 一个异步任务执行完成后，执行接下来的任务；
         * thenAccept/thenAcceptAsync: 可以感知上一步的返回值，但是不对返回值进行处理；
         * thenApply/thenApplyAsync: 可以感知上一步的返回值，并且对返回值进行二次处理；
         *
         * 两任务组合：两个任务均完成后，在执行的函数；
         * runAfterBoth/runAfterBothAsync: 无法获取当前两个任务的结果；
         * thenAcceptBoth/thenAcceptBothAsync: 可以获取到两个任务的返回值，但是没有返回结果；
         * thenCombine/thenCombineAsync: 可以获取两个任务的返回值，并返回自己的处理结果；
         *
         * 两任务组合：一个完成：
         * runAfterEither/runAfterEitherAsync：两个任务中一个完成则执行； 无法获取两个任务的结果;
         * acceptEither/acceptEitherAsync: 两个任务中一个完成则执行； 获取两个任务的结果;
         * applyToEither/applyToEitherAsync: 两个任务中一个完成则执行；并返回自己的处理结果；
         *
         * 多任务组合：
         * allof: 组合所有的completableFuture,全部都做完才执行；
         * anyOf: 任何一个任务完成，都会继续执行；
         */
        System.out.println("main ... start");
//        CompletableFuture<Void> async = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getName());
//        }, service);
//        CompletableFuture<String> async = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getName());
//            int i = 10/0;
//            return "lichenglin";
//        }, service).whenCompleteAsync((result,exception)->{
//            System.out.println("异步任务成功完成，结果是："+result + "异常是："+exception);
//        }).exceptionally(exception->{
//            // exceptionally可以将异常捕获，对异常信息进行二次包装。
//            return String.valueOf(10);
//        });

//        CompletableFuture<String> async = CompletableFuture.supplyAsync(() -> {
////            System.out.println("当前线程：" + Thread.currentThread().getName());
//////            int i = 10/0;
////        return "lichenglin";
////    }, service).handle((result,exception)->{
////        if(result != null){
////            return  result + ",jiayuou";
////        }
////        if(exception != null){
////            return  "0";
////        }
////        return "0";
////    });

        //由于任务1有返回值，而任务2没有返回值，最后返回值是由任务2来确定的，因此返回值类型为void;
        /**
         * 无法感知上一步的返回结果；
         * thenRunAsync(()->{
         *             System.out.println("任务2执行");
         *         },service);
         *可以获取上一步的返回结果，但是没有返回值；
         * thenAcceptAsync((result)->{
         *             System.out.println(result+"jiayou");
         *         },service);
         */
//        CompletableFuture<String> async1 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getName());
////            int i = 10/0;
//            return "lichenglin";
//        }, service).thenApplyAsync((result)->{
//            return result + "努力，就能成功";
//        },service);
//        System.out.println("====================");
//        System.out.println(async1.get());
//        System.out.println("main ... end");
//
//
//        CompletableFuture<String> async1 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getName());
////            int i = 10/0;
//            return "lichenglin";
//        }, service);
//
//        CompletableFuture<String> async2 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getName());
////            int i = 10/0;
//            return "jiayou";
//        }, service);
//
////        async1.thenAcceptBothAsync(async2,(result1,result2)->{
////            System.out.println(result1+ "==" + result2);
////        },service);
//
//        CompletableFuture<String> objectCompletableFuture = async1.thenCombineAsync(async2, (result1, result2) -> {
//            String result = result1 + result2 + "!!!!!!";
//            return result;
//        }, service);
//        System.out.println(objectCompletableFuture.get());


        CompletableFuture<String> async1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getName());
//            int i = 10/0;
            return "lichenglin";
        }, service);

        CompletableFuture<String> async2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("当前线程：" + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "lichenglin";
        }, service);

//        async1.runAfterEitherAsync(async2,()->{
////            System.out.println("有一个任务执行完毕，任务3开始执行");
////        },service);
        // 两个任务的返回类型必须一致；
//        async1.acceptEitherAsync(async2,(result1)->{
//            System.out.println(result1+"------>");
//        },service);

        CompletableFuture<String> async = async1.applyToEitherAsync(async2, (res) -> {
            return res + "====>";
        }, service);
        System.out.println(async.get());
    }
}
