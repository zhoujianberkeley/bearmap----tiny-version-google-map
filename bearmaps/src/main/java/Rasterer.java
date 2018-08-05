//import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    /** The max image depth level. */
    public static final int MAX_DEPTH = 7;

    // Values of the whole region
    double longitudeRightBound = MapServer.ROOT_LRLON;
    double longitudeLeftBound = MapServer.ROOT_ULLON;
    double latitudeUpBound = MapServer.ROOT_ULLAT;
    double latitudeLowBound = MapServer.ROOT_LRLAT;
    double longitudeSpan = MapServer.ROOT_LRLON - MapServer.ROOT_ULLON;
    double latitudeSpan = MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT;

    /**
     * Takes a user query and finds the grid of images that best matches the query. These images
     * will be combined into one big image (rastered) by the front end. The grid of images must obey
     * the following properties, where image in the grid is referred to as a "tile".
     * <ul>
     *     <li>The tiles collected must cover the most longitudinal distance per pixel (LonDPP)
     *     possible, while still covering less than or equal to the amount of longitudinal distance
     *     per pixel in the query box for the user viewport size.</li>
     *     <li>Contains all tiles that intersect the query bounding box that fulfill the above
     *     condition.</li>
     *     <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     * @param params The RasterRequestParams containing coordinates of the query box and the browser
     *               viewport width and height.
     * @return A valid RasterResultParams containing the computed results.
     */
    public RasterResultParams getMapRaster(RasterRequestParams params) {

        // Get the numbers in the request
        double upperLeftLatitude = params.ullat;
        double upperLeftLongitude = params.ullon;
        double lowerRightLatitude = params.lrlat;
        double lowerRightLongitude = params.lrlon;
        double width = params.w;
        double height = params.h;

        // things to put in RasterResultParams
        double rasterUlLon, rasterUlLat, rasterLrLon, rasterLrLat;
        boolean querySuccess;

        // calculate longitude distance per pixel of the request for comparision
        double requestLonDPP = lonDPP(lowerRightLongitude, upperLeftLongitude, width);

        // calculate the required depth of png files
        int depth = getDepth(requestLonDPP);
        int numOfBlocks = (int) Math.pow(2, depth);

        // the longitude-span of each block in the calculated depth
        double longitudePerBlock = longitudeSpan / (Math.pow(2, depth));
        double latitudePerBlock = latitudeSpan / ((Math.pow(2, depth)));

        // calculate the distance between the left bound and the output region
        int leftCount = leftBound(longitudePerBlock, upperLeftLongitude);
        int rightCount = rightBound(longitudePerBlock, lowerRightLongitude);
        int upCount = upBound(latitudePerBlock, upperLeftLatitude);
        int lowCount = lowBound(latitudePerBlock, lowerRightLatitude);

        // Create a render grid to put into RasterResultParams
        String[][] renderGrid =
                new String[numOfBlocks - upCount - lowCount][numOfBlocks - leftCount - rightCount];
        // Fill the renderGrid
        for (int i = upCount; i < numOfBlocks - lowCount; i++) {
            for (int j = leftCount; j < numOfBlocks - rightCount; j++) {
                renderGrid[i - upCount][j - leftCount] = toFile(depth, j, i);
            }
        }

        // calculate bounds for the return region
        rasterUlLon = longitudeLeftBound + leftCount * longitudePerBlock;
        rasterLrLon = longitudeRightBound - rightCount * longitudePerBlock;
        rasterUlLat = latitudeUpBound - upCount * latitudePerBlock;
        rasterLrLat = latitudeLowBound + lowCount * latitudePerBlock;

        RasterResultParams.Builder newBuilder = new RasterResultParams.Builder();
        newBuilder.setRenderGrid(renderGrid);
        newBuilder.setRasterLrLat(rasterLrLat);
        newBuilder.setRasterLrLon(rasterLrLon);
        newBuilder.setRasterUlLat(rasterUlLat);
        newBuilder.setRasterUlLon(rasterUlLon);
        newBuilder.setDepth(depth);
        newBuilder.setQuerySuccess(true);

        RasterResultParams toReturn = newBuilder.create();

        /*
        * To get the array of array of png files, first calculate the 4 bounds of the region
        * to return, and then put corresponding file names into the array of array.
        * */


        /* Make sure you can explain every part of the task before you begin.
         * Hint: Define additional classes to make it easier to pass around multiple values, and
         * define additional methods to make it easier to test and reason about code. */

        return toReturn;
        //return RasterResultParams.queryFailed();
    }

    /**
     * Calculates the lonDPP of an image or query box
     * @param lrlon Lower right longitudinal value of the image or query box
     * @param ullon Upper left longitudinal value of the image or query box
     * @param width Width of the query box or image
     * @return lonDPP
     */
    private double lonDPP(double lrlon, double ullon, double width) {
        return (lrlon - ullon) / width;
    }

    private int getDepth(double requestlonDPP) {
        int depth = 0;
        double lrLon = MapServer.ROOT_LRLON;
        double resultLonDPP;
        while (depth < 7) {
            resultLonDPP = lonDPP(lrLon, MapServer.ROOT_ULLON, 256);
            if (resultLonDPP < requestlonDPP) {
                return depth;
            }
            depth += 1;
            lrLon = (MapServer.ROOT_ULLON + lrLon) / 2;
        }
        return depth;
    }

    private int leftBound(double span, double leftRequest) {
        double start = longitudeLeftBound;
        int count = 0;
        while (count * span + start < leftRequest) {
            count += 1;
        }
        return count - 1;
    }

    private int rightBound(double span, double rightRequest) {
        double start = longitudeRightBound;
        int count = 0;
        while (start - count * span > rightRequest) {
            count += 1;
        }
        return count - 1;
    }

    private int upBound(double span, double upRequest) {
        double start = latitudeUpBound;
        int count = 0;
        while (start - count * span > upRequest) {
            count += 1;
        }
        return count - 1;
    }

    private int lowBound(double span, double lowRequest) {
        double start = latitudeLowBound;
        int count = 0;
        while (start + count * span < lowRequest) {
            count += 1;
        }
        return count - 1;
    }

    private String toFile(int d, int x, int y) {
        return "d" + d + "_x" + x + "_y" + y + ".png";
    }
}
