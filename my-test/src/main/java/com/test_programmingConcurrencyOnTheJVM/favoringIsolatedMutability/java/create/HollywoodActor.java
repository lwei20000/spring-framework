package com.test_programmingConcurrencyOnTheJVM.favoringIsolatedMutability.java.create;

import akka.actor.UntypedActor;

/**
 * @Auther: weiliang
 * @Date: 2021/6/14 11:05
 * @Description:
 */
public class HollywoodActor extends UntypedActor {
	public void onReceive(final Object role) {
		System.out.println("Playing " + role + " from Thread " + Thread.currentThread().getName());
	}
}
