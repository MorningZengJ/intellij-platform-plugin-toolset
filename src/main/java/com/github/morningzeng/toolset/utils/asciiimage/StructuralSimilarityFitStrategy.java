package com.github.morningzeng.toolset.utils.asciiimage;

/**
 * Calculates Structural Similarity index (SSIM) between the images.
 * <p>
 *
 * @see <a href="http://en.wikipedia.org/wiki/Structural_similarity">for more info.</a>
 */
public class StructuralSimilarityFitStrategy implements BestCharacterFitStrategy {

    @Override
    public float calculateError(GrayscaleMatrix character, GrayscaleMatrix tile) {

        float k1 = 0.01f;
        float l = 255f;
        float C1 = k1 * l;
        C1 *= C1;
        float k2 = 0.03f;
        float C2 = k2 * l;
        C2 *= C2;

        final int imgLength = character.getData().length;

        float score = 0f;
        for (int i = 0; i < imgLength; i++) {
            float pixelImg1 = character.getData()[i];
            float pixelImg2 = tile.getData()[i];

            score += (2 * pixelImg1 * pixelImg2 + C1) * (2 + C2)
                    / (pixelImg1 * pixelImg1 + pixelImg2 * pixelImg2 + C1) / C2;
        }

        // average and convert score to error
        return 1 - (score / imgLength);

    }

}