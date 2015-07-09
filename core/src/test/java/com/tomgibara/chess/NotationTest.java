package com.tomgibara.chess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class NotationTest {

	public String fileName;

	@Rule
	public TestRule watcher = new TestWatcher() {
		
		@Override
		public Statement apply(Statement base, Description description) {
			return super.apply(base, description);
		}
		
		protected void starting(Description description) {
			String methodName = description.getMethodName();
			fileName = methodName.startsWith("testGame") ? Character.toLowerCase(methodName.charAt(8)) + methodName.substring(9) + ".pgn" : null;
		}
	};

	@Test
	public void testGameBasic1() throws IOException {
		parse();
	}
	
	@Test
	public void testGameBasic2() throws IOException {
		parse();
	}
	
	@Test
	public void testGameSimple() throws IOException {
		Game game = parse();
		Assert.assertEquals(42 * 2 + 1 + 1, game.sequence.length() );
		Assert.assertEquals("1992.11.04", game.date().toString());
	}
	
	@Test
	public void testGameBackslash() throws IOException {
		Assert.assertEquals("Maurice\" (wh)", parse().white().get(0).firstName);
	}
	
	@Test
	public void testGameRounds() throws IOException {
		Assert.assertEquals(Arrays.asList(1,2), parse().round());
	}
	
	@Test
	public void testGameNames() throws IOException {
		Game game = parse();
		Assert.assertEquals("Short", game.white().get(0).lastName);
		Assert.assertEquals("Nigel", game.white().get(0).firstName);
		Assert.assertEquals("Vujatovic (wh)", game.white().get(1).lastName);
		Assert.assertEquals("", game.white().get(1).firstName);
	}
	
	private Game parse() {
		try (InputStream in = NotationTest.class.getResourceAsStream(fileName)) {
			return Notation.parse(new InputStreamReader(in, "ISO-8859-1"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
