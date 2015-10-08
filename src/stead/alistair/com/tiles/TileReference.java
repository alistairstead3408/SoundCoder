package stead.alistair.com.tiles;

import stead.alistair.com.soundcoder.R;

public class TileReference {

	public final static int TYPE_BLOB_ONE = 0, TYPE_BLOB_TWO = 1, TYPE_VAR = 2, TYPE_MUSIC = 3, TYPE_SYNTHID = 4,  TYPE_BLOB_PROPERTY = 5, TYPE_EMPTY = 6, TYPE_ATTACK = 7;
	
	public final static int CATAGORY_MODIFIER = 0, CATAGORY_PROPERTY = 1, CATAGORY_IDENTIFIER = 2, CATAGORY_GUARD = 3;

	public static String getTag(int resCode) {
		switch (resCode) {
		case R.drawable.tile_property_frequency:
			return "Pitch";
		case R.drawable.tile_modifier_attack:
			return "Time Value";
		case R.drawable.tile_blob_containment:
			return "Containment";
		case R.drawable.tile_blob_entrance:
			return "Entrance";
		case R.drawable.tile_blob_exit:
			return "Exit";
		case R.drawable.tile_blob_next:
			return "Next";
		case R.drawable.tile_blob_nextto:
			return "Next To";
		case R.drawable.tile_blob_true:
			return "True Tile";
		case R.drawable.tile_modifier_value:
			return "Value";
		case R.drawable.tile_modifier_area:
			return "Tile Area";
		case R.drawable.tile_property_volume:
			return "Volume";
		case R.drawable.tile_synth:
			return "Synth Identifier";
		case R.drawable.tile_blob_see:
			return "Visible Blob";
		default:
			return "Unknown: " + resCode;
		}

	}

	/** Allows us to get the type from any class */
	public static int getType(int resCode) {
		switch (resCode) {
		case R.drawable.tile_property_frequency:
		
		case R.drawable.tile_property_volume:
			return TileReference.TYPE_MUSIC;
		case R.drawable.tile_modifier_value:
			return TileReference.TYPE_VAR;
		case R.drawable.tile_blob_entrance:
		case R.drawable.tile_blob_see:
		case R.drawable.tile_blob_exit:
		case R.drawable.tile_blob_true:
			return TileReference.TYPE_BLOB_ONE;
		case R.drawable.tile_blob_containment:
		case R.drawable.tile_blob_next:
		case R.drawable.tile_blob_nextto:
			return TileReference.TYPE_BLOB_TWO;
		case R.drawable.tile_synth:
			return TileReference.TYPE_SYNTHID;
		case R.drawable.tile_modifier_area:
			return TileReference.TYPE_BLOB_PROPERTY;
		case R.drawable.tile_modifier_attack:
			return TileReference.TYPE_ATTACK;
		default:
			return TileReference.TYPE_EMPTY;
		}
	}
	 
	/** Allows us to get the type from any class */
	public static int getCatagory(int resCode) {
		switch (resCode) {
		case R.drawable.tile_property_frequency:
		case R.drawable.tile_property_volume:
			return TileReference.CATAGORY_PROPERTY;
		case R.drawable.tile_blob_entrance:
		case R.drawable.tile_blob_see:
		case R.drawable.tile_blob_exit:
		case R.drawable.tile_blob_containment:
		case R.drawable.tile_blob_next:
		case R.drawable.tile_blob_nextto:
		case R.drawable.tile_blob_true:
			return TileReference.CATAGORY_GUARD;
		case R.drawable.tile_modifier_area:
		case R.drawable.tile_modifier_attack:
		case R.drawable.tile_modifier_value:
			return TileReference.CATAGORY_MODIFIER;
		case R.drawable.tile_synth:
			return TileReference.CATAGORY_IDENTIFIER;
		default:
			return -1;
		}
	}
	
	
	
	public static String getSynthString(int synthNo){
		switch(synthNo){
		case 1:
			return "α";
		case 2:
			return "β";
		case 3:
			return "γ";
		case 4:
			return "δ";
		case 5:
			return "ε";
		case 6:
			return "ζ";
		case 7:
			return "η";
		case 8:
			return "θ";
		case 9:
			return "ι";
		case 10:
			return "κ";
		case 11:
			return "λ";
		case 12:
			return "μ";
		case 13:
			return "ν";
		case 14:
			return "ξ";
		case 15:
			return "ο";
		case 16:
			return "π";
		case 17:
			return "ρ";
		case 18:
			return "σ ";
		case 19:
			return "τ";
		case 20:
			return "υ";
		case 21:
			return "φ";
		case 22:
			return "ψ";
		case 23:
			return "X";
		case 24:
			return "ω";
		default:
			return "";
		}//Switch
	}
	
	

}
