package app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class PriorFrameAdaptiveVideoDecoder {

	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		String input_file_name = "video_data/encoder_output.txt";
		String output_file_name = "video_data/decoder_output.txt";

		FileInputStream fis = new FileInputStream(input_file_name);

		InputStreamBitSource bit_source = new InputStreamBitSource(fis);

		Integer[] symbols = new Integer[256];

		for (int i=0; i<256; i++) {
			symbols[i] = i;
		}

		FreqCountIntegerSymbolModel[] models = new FreqCountIntegerSymbolModel[256];

		for (int i=0; i<256; i++) {
			models[i] = new FreqCountIntegerSymbolModel(symbols);
		}

		int num_symbols = bit_source.next(32);

		int range_bit_width = bit_source.next(8);

		ArithmeticDecoder<Integer> decoder = new ArithmeticDecoder<Integer>(range_bit_width);

		System.out.println("Uncompressing file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);
		System.out.println("Number of encoded symbols: " + num_symbols);

		FileOutputStream fos = new FileOutputStream(output_file_name);

		FreqCountIntegerSymbolModel model = models[0];

		int[][] frames = new int[300][4096];

		int frameIndex = 0;

		for (int i=0; i<num_symbols; i++) {

			if(i % 4096 == 0 && i > 4095) {
				frameIndex++;
			}

			if(frameIndex > 0) {
				model = models[frames[frameIndex-1][i-(frameIndex*4096)]];
			}

			int sym = decoder.decode(model, bit_source);
			frames[frameIndex][i-(frameIndex*4096)] = sym;

			if(sym != -1) {
				fos.write(sym);
			} 

			model.addToCount(sym);

		}

		System.out.println("Done.");
		fos.flush();
		fos.close();
		fis.close();

	}
}
