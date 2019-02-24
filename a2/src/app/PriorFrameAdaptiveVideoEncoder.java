package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticEncoder;
import io.OutputStreamBitSink;

public class PriorFrameAdaptiveVideoEncoder {

	public static void main(String[] args) throws IOException {
		String input_file_name = "video_data/uncompressed_input.txt";
		String output_file_name = "video_data/encoder_output.txt";

		int range_bit_width = 40;

		System.out.println("Encoding text file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);

		int num_symbols = (int) new File(input_file_name).length();

		Integer[] symbols = new Integer[256];
		for (int i=0; i<256; i++) {
			symbols[i] = i;
		}

		FreqCountIntegerSymbolModel[] models = new FreqCountIntegerSymbolModel[256];

		for (int i=0; i<256; i++) {
			models[i] = new FreqCountIntegerSymbolModel(symbols);
		}

		ArithmeticEncoder<Integer> encoder = new ArithmeticEncoder<Integer>(range_bit_width);

		FileOutputStream fos = new FileOutputStream(output_file_name);
		OutputStreamBitSink bit_sink = new OutputStreamBitSink(fos);

		bit_sink.write(num_symbols, 32);		

		bit_sink.write(range_bit_width, 8);

		FileInputStream fis = new FileInputStream(input_file_name);

		FreqCountIntegerSymbolModel model = models[0];
		int[][] frames = new int[300][4096];
		int frameIndex = 0;
		for (int i=0; i<num_symbols; i++) {

			if(i % 4096 == 0 && i > 4095) {
				frameIndex++;
			}

			int next_symbol = fis.read();

			frames[frameIndex][i-(frameIndex*4096)] = next_symbol;

			if(frameIndex > 0) {
				model = models[frames[frameIndex-1][i-(frameIndex*4096)]];
			}

			encoder.encode(next_symbol, model, bit_sink);

			model.addToCount(next_symbol);

		}

		fis.close();

		encoder.emitMiddle(bit_sink);
		bit_sink.padToWord();
		fos.close();

		System.out.println("Done");
	}
}
