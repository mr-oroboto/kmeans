package net.oroboto.kmeans;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

import net.oroboto.kmeans.model.Point;

public class TestPoint
{
    @Test
    public void testConstructionWithListOfDouble()
    {
        List<Double> list = new ArrayList();
        list.add(new Double(1.2));

        Point p = new Point(list);
    }

}