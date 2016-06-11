package org.hashids;

import java.util.Arrays;

import org.junit.Test;

import junit.framework.Assert;

public class HashidsTest {
	
	@Test
	public void testCase1(){
		long n = Long.MAX_VALUE;
		Hashids h = new Hashids("this is my salt", 8);
		Assert.assertEquals(n, h.decode(h.encode(n))[0]);
	}
	
	@Test
	public void testCase2(){
		long[] n = {0l, 1l, 2l, 3l, 4l, 5l, 6l, 7l, 8l, 9l};
		Hashids h = new Hashids("this is my salt", 8);
		long[] decoded = h.decode(h.encode(n));
		Assert.assertEquals(n.length, decoded.length);
		Assert.assertTrue(Arrays.equals(n, decoded));
	}
	
	@Test
	public void testCase3(){
		long[] n = new long[1001];
		for(int i = 0; i <= 1000; i++) n[i] = (long)i;
		Hashids h = new Hashids("ThisIsMySalt", 8);
		long[] decoded = h.decode(h.encode(n));
		Assert.assertEquals(n.length, decoded.length);
		Assert.assertTrue(Arrays.equals(n, decoded));
	}
	
	@Test
	public void testCase4(){
		Hashids h = new Hashids("this is my salt", 8);
		for(int n = 0; n <= 1000; n++){
			Assert.assertEquals(n, h.decode(h.encode(n))[0]);
		}
	}
	
	@Test
	public void testCase5(){
		Hashids h = new Hashids("this is my salt");
		Assert.assertEquals(h.encode(0), "5x");
		Assert.assertEquals(h.encode(1), "NV");
		Assert.assertEquals(h.encode(2), "6m");
		Assert.assertEquals(h.encode(3), "yD");
		Assert.assertEquals(h.encode(4), "2l");
		Assert.assertEquals(h.encode(5), "rD");
		Assert.assertEquals(h.encode(6), "lv");
		Assert.assertEquals(h.encode(7), "jv");
		Assert.assertEquals(h.encode(8), "D1");
		Assert.assertEquals(h.encode(9), "Qg");
	}
	
	@Test
	public void testCase6(){
		Hashids h = new Hashids("this is my salt", 0, "abcdefghijklmnopqrstuvwxyz");
		long[] n = {0l, 1l, 2l, 3l, 4l, 5l, 6l, 7l, 8l, 9l};
		Assert.assertTrue(Arrays.equals(n, h.decode(h.encode(n))));
	}
	
	@Test
	public void testCase7(){
		Hashids h = new Hashids("this is my salt", 0, "abcdefgh12345678");
		long[] n = {0l, 1l, 2l, 3l, 4l, 5l, 6l, 7l, 8l, 9l};
		Assert.assertTrue(Arrays.equals(n, h.decode(h.encode(n))));
	}
	
	@Test
	public void testCase8(){
		long[] n = new long[100001];
		for(int i = 0; i <= 100000; i++) n[i] = (long)i;
		Hashids h = new Hashids("ThisIsMyMixedCaseSalt", 4, "abcdefgh12345678");
		long[] decoded = h.decode(h.encode(n));
		Assert.assertEquals(n.length, decoded.length);
		Assert.assertTrue(Arrays.equals(n, decoded));
	}
	
}
