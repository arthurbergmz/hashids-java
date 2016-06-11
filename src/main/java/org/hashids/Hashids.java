package org.hashids;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unofficial version of Hashids
 * 
 * Based on the work of Ivan Akimov
 * https://github.com/ivanakimov/hashids.js
 * 
 * @author Ivan Akimov
 * @author Arthur Arioli Bergamaschi
 */
public class Hashids {
	
	private static final Pattern HEX_TESTER = Pattern.compile("^[0-9a-fA-F]+$");
	private static final Pattern HEX_MATCHER = Pattern.compile("([\\w\\W]{1,12})");
	private static final Pattern EMPTY_PATTERN = Pattern.compile("\\s+");
	private static final String ALPHANUMERIC_SEQUENCE = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	
	private String salt, alphabet, guards, seps = "cfhistuCFHISTU";
	private int alphabetLength, minHashLength, guardsLength, sepsLength, minAlphabetLength = 16;
	
	public Hashids(String salt){
		this(salt, 0, ALPHANUMERIC_SEQUENCE);
	}
	
	public Hashids(String salt, int minHashLength){
		this(salt, minHashLength, ALPHANUMERIC_SEQUENCE);
	}
	
	public Hashids(String salt, int minHashLength, String alphabet){
		this.salt = salt;
		this.minHashLength = (minHashLength < 0) ? 0 : minHashLength;
		this.alphabet = alphabet;
		this.alphabetLength = 0;
		StringBuilder uniqueAlphabet = new StringBuilder();
		for(int i = 0, j = this.alphabet.length(); i < j; i++){
			char c = this.alphabet.charAt(i);
			if(c == ' ') throw new IllegalArgumentException("error: alphabet cannot contain spaces");
			if(uniqueAlphabet.indexOf(String.valueOf(c)) == -1){
				uniqueAlphabet.append(c);
				this.alphabetLength++;
			}
		}
		if(this.alphabetLength < this.minAlphabetLength) throw new IllegalArgumentException("error: alphabet must contain at least " + this.minAlphabetLength + " unique characters");
		this.alphabet = uniqueAlphabet.toString();
		for(int i = 0, j = this.seps.length(); i < j; i++){
			int indexOf = this.alphabet.indexOf(this.seps.charAt(i));
			if(indexOf == -1){
				this.seps = this.seps.substring(0, i) + " " + this.seps.substring(i + 1);
				continue;
			}
			this.alphabet = this.alphabet.substring(0, indexOf) + " " + this.alphabet.substring(indexOf + 1);
		}
		this.alphabet = EMPTY_PATTERN.matcher(this.alphabet).replaceAll("");
		this.alphabetLength = this.alphabet.length();
		this.seps = EMPTY_PATTERN.matcher(this.seps).replaceAll("");
		this.seps = this.consistentShuffle(this.seps, this.salt);
		this.sepsLength = this.seps.length();
		double sepDiv = 3.5;
		if(this.sepsLength == 0 || (((float)this.alphabetLength) / this.sepsLength) > sepDiv){
			int sepsLength = (int)Math.ceil(this.alphabetLength / sepDiv);
			if(sepsLength == 1) sepsLength++;
			if(sepsLength > this.sepsLength){
				int diff = sepsLength - this.sepsLength;
				this.seps += this.alphabet.substring(0, diff);
				this.sepsLength = this.seps.length();
				this.alphabet = this.alphabet.substring(diff);
				this.alphabetLength = this.alphabet.length();
			}else{
				this.seps = this.seps.substring(0, sepsLength);
				this.sepsLength = this.seps.length();
			}
		}
		this.alphabet = this.consistentShuffle(this.alphabet, this.salt);
		int guardDiv = 12, guardCount = (int)Math.ceil(this.alphabetLength / guardDiv);
		if(this.alphabetLength > 3){
			this.guards = this.alphabet.substring(0, guardCount);
			this.guardsLength = this.guards.length();
			this.alphabet = this.alphabet.substring(guardCount);
			this.alphabetLength = this.alphabet.length();
		}else{
			this.guards = this.seps.substring(0, guardCount);
			this.guardsLength = this.guards.length();
			this.seps = this.seps.substring(guardCount);
			this.sepsLength = this.seps.length();
		}
	}
	
	public String encodeHex(String str){
		if(!HEX_TESTER.matcher(str).matches()) return "";
		Matcher matcher = HEX_MATCHER.matcher(str);
		matcher.matches();
		int size = matcher.groupCount();
		if(size == 0) return "";
		long[] numbers = new long[size];
		for(int i = 0; i < size; i++) numbers[i] = Long.parseLong("1" + matcher.group(i), 16);
		return this.encode(numbers);
	}
	
	public String decodeHex(String hash){
		StringBuilder builder = new StringBuilder();
		long[] numbers = this.decode(hash);
		for(int i = 0, j = numbers.length; i < j; i++) builder.append(Long.toHexString(numbers[i]).substring(1));
		return builder.toString();
	}
	
	public String encode(long... numbers){
		int numbersSize = numbers.length;
		if(numbersSize == 0) return "";
		int i, numbersHashInt = 0;
		for(i = 0; i < numbersSize; i++) numbersHashInt += (numbers[i] % (i + 100));
		String alphabet = this.alphabet;
		char lottery = alphabet.charAt(numbersHashInt % this.alphabetLength);
		StringBuilder builder = new StringBuilder(String.valueOf(lottery));
		for(i = 0; i < numbersSize; i++){
			long number = numbers[i];
			String buffer = lottery + this.salt + alphabet;
			alphabet = this.consistentShuffle(alphabet, buffer.substring(0, this.alphabetLength));
			String last = this.hash(number, alphabet);
			builder.append(last);
			if((i + 1) < numbersSize){
				number %= (((int)last.charAt(0)) + i);
				builder.append(this.seps.charAt((int)(number % this.sepsLength)));
			}
		}
		int builderLength = builder.length();
		if(builderLength < this.minHashLength){
			builder.insert(0, this.guards.charAt((numbersHashInt + ((int)builder.charAt(0))) % this.guardsLength));
			if(++builderLength < this.minHashLength) builder.append(this.guards.charAt((numbersHashInt + ((int)builder.charAt(2))) % this.guardsLength));

		}
		int halfLength = this.alphabetLength / 2;
		String response = builder.toString();
		while(builder.length() < this.minHashLength){
			alphabet = this.consistentShuffle(alphabet, alphabet);
			builder.insert(0, alphabet.substring(halfLength)).append(alphabet.substring(0, halfLength));
			int excess = builder.length() - this.minHashLength;
			if(excess > 0){
				i = excess / 2;
				response = builder.substring(i, this.minHashLength + i);
			}
		}
		return response;
	}
	
	public long[] decode(String hash){
		String alphabet = this.alphabet;
		String hashBreakdown = hash.replaceAll("[" + this.guards + "]", " ");
		String[] hashArray = hashBreakdown.split(" ");
		int hashArrayLength = hashArray.length;
		hashBreakdown = hashArray[(hashArrayLength == 3 || hashArrayLength == 2) ? 1 : 0];
		char lottery = hashBreakdown.charAt(0);
		hashBreakdown = hashBreakdown.substring(1).replaceAll("[" + this.seps + "]", " ");
		hashArray = hashBreakdown.split(" ");
		hashArrayLength = hashArray.length;
		long[] numbers = new long[hashArrayLength];
		for(int i = 0; i < hashArrayLength; i++){
			alphabet = this.consistentShuffle(alphabet, (lottery + this.salt + alphabet).substring(0, this.alphabetLength));
			numbers[i] = this.unhash(hashArray[i], alphabet);
		}
		if(!this.encode(numbers).equals(hash)) numbers = new long[0];
		return numbers;
	}
	
	private String consistentShuffle(String alphabet, String salt){
		int integer, j, saltLength = salt.length();
		if(saltLength < 1) return alphabet;
		for(int i = alphabet.length() - 1, v = 0, p = 0; i > 0; i--, v++){
			v %= saltLength;
			p += integer = (int) salt.charAt(v);
			j = (integer + v + p) % i;
			char c = alphabet.charAt(j);
			alphabet = alphabet.substring(0, j) + alphabet.charAt(i) + alphabet.substring(j + 1);
			alphabet = alphabet.substring(0, i) + c + alphabet.substring(i + 1);
		}
		return alphabet;
	}
	
	private String hash(long input, String alphabet){
		StringBuilder builder = new StringBuilder();
		int alphabetLength = alphabet.length();
		do{
			builder.insert(0, alphabet.charAt((int)(input % alphabetLength)));
			input /= alphabetLength;
		}while(input > 0);
		return builder.toString();
	}
	
	private long unhash(String input, String alphabet){
		long number = 0, pos;
		for(int i = 0, j = input.length(); i < j; i++){
			pos = alphabet.indexOf(input.charAt(i));
			number += pos * Math.pow(alphabet.length(), (input.length() - i - 1));
		}
		return number;
	}
	
}
