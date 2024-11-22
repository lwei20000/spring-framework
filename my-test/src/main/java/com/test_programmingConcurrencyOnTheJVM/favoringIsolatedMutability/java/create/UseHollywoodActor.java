package com.test_programmingConcurrencyOnTheJVM.favoringIsolatedMutability.java.create;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.BalancingPool;

/**
 * @Auther: weiliang
 * @Date: 2021/6/14 11:13
 * @Description:
 */
public class UseHollywoodActor {
	public static void main(String[] args) throws InterruptedException {
		//生成角色系统
		ActorSystem system = ActorSystem.create("msgSystem");

		//生成角色 ProduceMsgActor
		final ActorRef johnnyDepp = system.actorOf(new BalancingPool(3).props(Props.create(HollywoodActor.class)),"HollywoodActor");

		johnnyDepp.tell("Jack Sparrow",ActorRef.noSender());
		Thread.sleep(100);
		johnnyDepp.tell("Edward Scissorhands",ActorRef.noSender());
		Thread.sleep(100);
		johnnyDepp.tell("Willy Wonka",ActorRef.noSender());
	}
}
