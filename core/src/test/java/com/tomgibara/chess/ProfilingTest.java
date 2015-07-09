package com.tomgibara.chess;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class ProfilingTest {
	
	public static void main(String[] args) throws IOException {
		test(args[0]);
	}
	
	private static void test(String fileName) throws IOException {
		int gameCount = 0;
		int lineCount = 0;
		String lastDetails = null;
		try (BR br = new BR( new InputStreamReader( new FileInputStream(fileName)))) {
			long start = System.currentTimeMillis();
			while (!br.eof) {
				Game game = null;
				try {
					game = Notation.parse(br);
				} catch (RuntimeException e) {
					System.err.println("Line of errored game: " + lineCount);
					System.err.println("Previous game: " + lastDetails);
					e.printStackTrace();
					// find a first move
					for (String line = br.readLine(); line != null && !line.startsWith("1."); line = br.readLine());
					// find a blank line
					for (String line = br.readLine(); line != null && !line.isEmpty(); line = br.readLine());
				}
				if (game != null) {
					gameCount++;
					lineCount = br.lineCount;
					lastDetails = "Event: " + game.event() + " White: " + game.white() + " Black: " + game.black();
					if ((gameCount % 1000) == 0) {
						long time = System.currentTimeMillis() - start;
						System.out.println("Games processed: " + gameCount + " Lines processed: " + lineCount + " Seconds: " + (time / 1000.0));
					}
				}
			}
		} finally {
			System.out.println("Parsed: " + gameCount + " to line " + lineCount);
			System.out.println(lastDetails);
		}
	}
	
	private static class BR extends BufferedReader {
		
		int lineCount = 0;
		boolean eof;
		
		BR(Reader reader) {
			super(reader);
		}
		
		@Override
		public String readLine() throws IOException {
			String line = super.readLine();
			eof = line == null;
			if (!eof) lineCount ++;
			return line;
		}
		
	}
	
}
