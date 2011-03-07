package FIT_8201_Sviridov_Weil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Class incapsulating polygon with such properties as: color, thickness and points
 * Based on <code>Point2D.Double</code>
 * Orientation test is based on <link>http://en.wikipedia.org/wiki/Curve_orientation</link>
 * @author alstein
 */
public class Polygon {

    public static final int CLOCKWISE_ORIENTATION = -1;
    public static final int COUNTERCLOCKWISE_ORIENTATION = 1;
    public static final double INF_POINT_COORD = 10E6;
    private int _orientation;
    private List<Point2D> _points = new ArrayList<Point2D>();
    Stroke _stroke = null;
    private Color _color;
    private int _thickness;

    /**
     * Returns last point of the <code>Polygon</code>
     * @return last point of the <code>Polygon</code>
     */
    public Point lastPoint() {
        int size = _points.size();
        if (size < 1) {
            return null;
        }

        Point2D p2d = _points.get(size - 1);
        return new Point((int) (p2d.getX() + 0.5), (int) (p2d.getY() + 0.5));
    }

    /**
     * Returns first point of the <code>Polygon</code>
     * @return first point of the <code>Polygon</code>
     */
    public Point firstPoint() {
        int size = _points.size();
        if (size < 1) {
            return null;
        }

        Point2D p2d = _points.get(0);

        return new Point((int) (p2d.getX() + 0.5), (int) (p2d.getY() + 0.5));
    }

    /**
     * Draws line from first and last points to given point <code>p</code> (intended to be used for rubber line)
     * @param g2
     * @param p point of rubber line ending
     */
    public void drawLine(Graphics2D g2, Point p) {
        int size = _points.size();
        if (size == 0) {
            return;
        }

        if (_stroke == null) {
            _stroke = new BasicStroke(_thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }

        g2.setColor(_color);
        g2.setStroke(_stroke);


        if (size == 1) {
            Point fp = firstPoint();
            g2.drawLine(fp.x, fp.y, p.x, p.y);
            return;
        }

        Point fp = firstPoint();
        Point lp = lastPoint();

        g2.drawLine(fp.x, fp.y, p.x, p.y);
        g2.drawLine(p.x, p.y, lp.x, lp.y);
    }

    /**
     * Draws all segments but the ones to be drawn with rubber line
     * @param g2
     */
    public void drawPartial(Graphics2D g2) {
        int size = _points.size();
        if (size < 2) {
            return;
        }

        if (_stroke == null) {
            _stroke = new BasicStroke(_thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }

        g2.setColor(_color);
        g2.setStroke(_stroke);

        for (int i = 1; i < size; ++i) {
            int x1, x2, y1, y2;

            Point2D p1 = _points.get(i - 1);
            Point2D p2 = _points.get(i);

            x1 = (int) (p1.getX() + 0.5);
            y1 = (int) (p1.getY() + 0.5);
            x2 = (int) (p2.getX() + 0.5);
            y2 = (int) (p2.getY() + 0.5);

            g2.drawLine(x1, y1, x2, y2);
        }


    }

    /**
     * Determines if the given point (x,y) lies inside of the Polygon
     * using even-odd method
     * @param x x coordinate
     * @param y y coordinate
     * @return true if point lies inside, false otherwise
     */
    public boolean isInside(int x, int y) {
        return isInside(new Point2D.Double(x, y));
    }

    /**
     * Determines if the given point <code>p</code> lies inside of the Polygon
     * using even-odd method
     * @param p point to check
     * @return true if point lies inside, false otherwise
     */
    public boolean isInside(Point2D p) {
        int size = _points.size();
        if (size < 2) {
            return false;
        }

        int intersections_number = 0;
        // selecting direction
        Point2D dp = new Point2D.Double(INF_POINT_COORD, INF_POINT_COORD);

        for (int i = 1; i < size + 1; ++i) {
            Point2D p1 = _points.get(i - 1);
            Point2D p2;

            if (i == size) {
                p2 = _points.get(0);
            } else {
                p2 = _points.get(i);
            }
            Point2D intersection_point = EuclideanGeometry.getIntersection(p, dp, p1, p2);

            if (intersection_point != null) {
                intersections_number++;
            }
        }

        if (intersections_number % 2 == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checks if given Polygon lies within another:
     * at first all points of the given polygon are checked to be inside
     * and if true intersections are checked with odd-even
     * method
     * @param another Polygon to test
     * @return true if
     */
    public boolean isInside(Polygon another) {
        if (this.isEmpty() || another.isEmpty()) {
            return false;
        }

        for (Point2D p : another._points) {
            if (!this.isInside(p)) {
                return false;
            }
        }

        int another_size = another._points.size();
        int this_size = _points.size();

        for (int i = 1; i < another_size + 1; ++i) {
            Point2D a1 = another._points.get(i - 1);
            Point2D a2 = null;
            if (i == another_size) {
                a2 = another._points.get(0);
            } else {
                a2 = another._points.get(i);
            }

            for (int j = 1; j < this_size + 1; ++j) {
                Point2D t1 = _points.get(j - 1);
                Point2D t2 = null;
                if (j == this_size) {
                    t2 = _points.get(0);
                } else {
                    t2 = _points.get(j);
                }

                if (EuclideanGeometry.getIntersection(a1, a2, t1, t2) != null) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Constructor with out parameters
     * color and thickness field are set to default values black and 1
     * @param orientation orientation of Polygon (CLOCLWISE and COUNTERCLOCKWISE)
     */
    public Polygon(int orientation) {
        _orientation = orientation;
        _color = Color.black;
        _thickness = 1;
    }

    /**
     * Constructor with given color and thickness
     * @param orientation orientation of Polygon (CLOCLWISE and COUNTERCLOCKWISE)
     * @param color inital color
     * @param thickness initial thickness
     */
    public Polygon(int orientation, Color color, int thickness) {
        _orientation = orientation;
        _color = new Color(color.getRGB());
        _thickness = thickness;
    }

    /**
     * Clears all points of the polygon
     */
    public void clear() {
        _points.clear();
    }

    /**
     * Replaces points with <code>p</code> Polygon's points
     */
    public void setPoints(Polygon p) {
        this._points = p._points;
    }

    /**
     * Adds another point to the <code>Polygon</code>
     * @param x x coordinate
     * @param y y coordinate
     */
    public void addPoint(int x, int y) {
        addPoint(new Point2D.Double(x, y));
    }

    /**
     * Adds another point to the <code>Polygon</code>
     * @param p new point to be added
     */
    public void addPoint(Point2D p) {
        _points.add(p);
    }

    /**
     * Draws Polygon on the given <code>Graphics2D</code> object
     * @param g2 Graphics2D object to draw on
     */
    public void draw(Graphics2D g2) {
        if (_stroke == null) {
            _stroke = new BasicStroke(_thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }

        g2.setColor(_color);
        g2.setStroke(_stroke);

        int size = _points.size();

        for (int i = 1; i < size + 1; ++i) {
            int x1, x2, y1, y2;

            Point2D p1 = _points.get(i - 1);
            Point2D p2;

            if (i == size) {
                p2 = _points.get(0);
            } else {
                p2 = _points.get(i);
            }

            x1 = (int) (p1.getX() + 0.5);
            y1 = (int) (p1.getY() + 0.5);
            x2 = (int) (p2.getX() + 0.5);
            y2 = (int) (p2.getY() + 0.5);

            g2.drawLine(x1, y1, x2, y2);
        }

    }

    /**
     * Returns current thickness of Polygon
     * @return current thickness of Polygon
     */
    public int getThickness() {
        return _thickness;
    }

    /**
     * Sets thickness of Polygon
     * @param thickness new thickness of Polygon
     */
    public void setThickness(int thickness) {
        if (thickness != _thickness) {
            _stroke = new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }
        _thickness = thickness;
    }

    /**
     * Returns current color of Polygon
     * @return current color of Polygon
     */
    public Color getColor() {
        return _color;
    }

    /**
     * Sets color of Polygon
     * @param color new color of Polygon
     */
    public void setColor(Color color) {
        _color = new Color(color.getRGB());
    }

    /**
     * Returns polygon object in standard representation: number of points,
     * coordinates of points
     * @return String with standard polyline representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(_points.size());
        sb.append("\r\n");

        for (Point2D p : _points) {
            int x = (int) (p.getX() + 0.5);
            int y = (int) (p.getY() + 0.5);

            sb.append(x);
            sb.append(" ");
            sb.append(y);
            sb.append("\r\n");
        }

        return sb.toString();
    }

    /**
     * Transforms Polygon to OrientedVertex structure
     * @return if Polygon is not empty returns OrientedVertex structure, null otherwise
     */
    public OrientedVertex getFirstOrientedVertex() {

        if (_points.isEmpty()) {
            return null;
        }

        OrientedVertex first = null;
        OrientedVertex v = null;
        int size = _points.size();

        for (int i = 0; i < size + 1; ++i) {
            if (i == 0) {
                first = new OrientedVertex(_points.get(0), true);
                v = first;
            }

            if (i > 0 && i < size) {
                v.setNext(new OrientedVertex(_points.get(i)));
                v = v.getNext();
            }

            if (i == size) {
                v.setNext(first);
            }
        }

        return first;
    }

    /**
     * Returns number of points in Polygon
     * @return number of points in Polygon
     */
    public int verticesCount() {
        return _points.size();
    }

    /**
     * Returns true if there are no vertices in Polygon, false otherwise
     * @return true if there are no vertices in Polygon, false otherwise
     */
    public boolean isEmpty() {
        return _points.isEmpty();
    }

    /**
     * Computes the sign of 'cross product' used to
     * determine orientation
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     * @return -1 if 'cross product' is negative, +1 otherwise
     */
    private int crossProductSign(Point2D p1, Point2D p2, Point2D p3) {
        // (x_b - x_a)(y_c-y_a) - (x_c-x_a)(y_b-y_a) > 0 => COUNTERCLOCKWISE
        // -//- < 0 => CLOCKWISE
        // = 0 => on one line

        double xa = p1.getX(), ya = p1.getY(),
                xb = p2.getX(), yb = p2.getY(),
                xc = p3.getX(), yc = p3.getY();

        double product = (xb - xa) * (yc - ya) - (xc - xa) * (yb - ya);

        return (int) product;
    }

    /**
     * Tests if given point doesn't contradict to polygon orientation (considering last and first points ONLY)
     * In this implementation points lying on one line are forbidden
     * Also self-enterying leads to false
     * @return true is point is valid to be next point, false otherwise
     */
    public boolean isPointValid(Point2D p) {
        int size = _points.size();

        if (size <= 1) {
            return true;
        }

        Point2D first = _points.get(0);
        Point2D second = _points.get(1);
        Point2D last = _points.get(size - 1);
        Point2D prelast = _points.get(size - 2);


        if (crossProductSign(p, first, second) * _orientation <= 0
                && crossProductSign(prelast, last, p) * _orientation <= 0) {
            return false;
        }

        // self-entering checking
        for (int i = 1; i < size - 2; ++i) {
            Point2D p3 = _points.get(i);
            Point2D p4 = _points.get(i + 1);

            if (EuclideanGeometry.getIntersection(last, p, p3, p4) != null) {
                return false;
            }
        }

        if (size > 2 && (EuclideanGeometry.getIntersection(last, p, first, second) != null)) {
            return false;
        }

        return true;
    }

    /**
     * Test if polygon has no self intersections and if so returns true, false otherwise
     * @return true if polygon is OK, false otherwise
     */
    public boolean isFinished() {
        int size = _points.size();

        if (size == 3) {
            return true;
        }

        Point2D first = _points.get(0);
        Point2D second = _points.get(1);
        Point2D last = _points.get(size - 1);
        Point2D prelast = _points.get(size - 2);
        Point2D preprelast = _points.get(size - 3);
        // self-entering checking
        for (int i = 1; i < size - 3; ++i) {
            Point2D p3 = _points.get(i);
            Point2D p4 = _points.get(i + 1);

            if (EuclideanGeometry.getIntersection(first, last, p3, p4) != null
                    || EuclideanGeometry.getIntersection(last, prelast, p3, p4) != null) {
                return false;
            }
        }

        if (EuclideanGeometry.getIntersection(first, second, last, prelast) != null
                || EuclideanGeometry.getIntersection(last, first, prelast, preprelast) != null) {
            return false;
        }

        return true;
    }
}
