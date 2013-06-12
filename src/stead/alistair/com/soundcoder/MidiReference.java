package stead.alistair.com.soundcoder;

public class MidiReference {
	
	
	/** Using this method, start = 12, end = 112 (octave=12)*/
	public static String getNote(int percentage){
		String noteString = getOctaveNote(percentage % 12);
		noteString = noteString + ((percentage / 12));
		return noteString;
	}
	
	
	private static String getOctaveNote(int numberWithinOctave){
		switch(numberWithinOctave){
		case 0:
			return "C";
		case 1:
			return "C#";
		case 2:
			return "D";
		case 3:
			return "D#";
		case 4:
			return "E";
		case 5:
			return "F";
		case 6:
			return "F#";
		case 7:
			return "G";
		case 8:
			return "G#";
		case 9:
			return "A";
		case 10:
			return "A#";
		case 11:
			return "B";
		}
		return "-";
		
	}
	
}
