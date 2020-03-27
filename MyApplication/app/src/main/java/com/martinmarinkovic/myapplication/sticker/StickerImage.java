package com.martinmarinkovic.myapplication.sticker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import com.martinmarinkovic.myapplication.R;
import com.martinmarinkovic.myapplication.sticker.polygon.Point;
import com.martinmarinkovic.myapplication.sticker.polygon.Polygon;
import com.martinmarinkovic.myapplication.wallpaper.AddWallpaperFragment;


public class StickerImage extends View {

private static final int INVALID_POINTER_ID = -1;

    float min_size;
    float max_size;

    private Drawable _Image;
    private float _PosX;
    private float _PosY;

    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId = INVALID_POINTER_ID;

    private ScaleGestureDetector _ScaleDetector;
    private float _ScaleFactor = 1.f;
    private float _LastAngle = 0f;
    //private int  drawableHeight,drawableWidthCanvas,drawableHeightCanvas;
    //private int viewWidth, viewHeight;
    private Polygon polygon,polygonTemp;
    
    // when image is scaled, we use this to calculate the bounds of the image
    private int _ImageWidthScaled;
    private int _ImageHeightScaled;
    
    private RotateGestureDetector _RotateDetector;
    private RotateGestureDetector.OnRotationGestureListener _Listener;
    
   // this is to tell Style what view number I am in the array.
    public int _NumberView;
   
	private boolean _Selected = false,_Flipped = false;
    private Paint _BorderLeftLine;
    private Paint _BorderTopLine;
    private Paint _BorderRightLine;
    private Paint _BorderBottomLine;
    Paint _TestLine;
    
 // width and height of original image
    private float _ImageWidth;
    private float _ImageHeight;
    private float kx, ky;

    //TODO: Editor je aktivnost u kojoj se nalaze stikeri
	private AddWallpaperFragment mStyle;
	
	private Point a =  new Point(0,0);
	private Point b=  new Point(0,0);
	private Point c=  new Point(0,0);
	private Point d=  new Point(0,0);
	private final Point[] points = new Point[4];

    public StickerImage(Context context, String name, int count, AddWallpaperFragment style, int topX, int topY) {
      this(context, null, 0);
      //_Image = Drawable.createFromPath(uri);
        
      _Image = getResources().getDrawable( (getResources().getIdentifier(name, "drawable", context.getPackageName())));
      _Image.setBounds(0, 0, _Image.getIntrinsicWidth(), _Image.getIntrinsicHeight());
      _ImageWidth = _Image.getIntrinsicWidth();
      _ImageHeight = _Image.getIntrinsicHeight();
      _ImageWidthScaled = (int) (_ImageWidth*_ScaleFactor);
      _ImageHeightScaled = (int) (_ImageHeight*_ScaleFactor);
      this._NumberView = count;
      this.mStyle = style;

        //TODO: dodaj stringove sa vrednostima u resurse
        min_size = Float.parseFloat(context.getString(R.string.min_scale));
        max_size = Float.parseFloat(context.getString(R.string.max_scale));

      if(topX!=0 && topY!=0)
      {
    	  _PosX = _PosX+topX-_ImageWidth/2;
    	  _PosY = _PosY+topY-_ImageHeight/2;
      }
      
      a.x = 0;
      a.y = 0;
      b.x = (int) _ImageWidth; 
      b.y = 0+ky;
      c.x = (int) _ImageWidth;
      c.y = (int) _ImageHeight;
      d.x = 0+kx;
      d.y = (int) _ImageHeight;
      points[0] = a;
      points[1] = b;
      points[2] = c;
      points[3] = d;
      polygonTemp = Polygon.Builder()
      	    .addVertex(a)
      	    .addVertex(b)
      	    .addVertex(c)
      	    .addVertex(d)
      	    .build();
      init(context);
      
      if(topX!=0 && topY!=0)
      {
    	   a.x = _PosX;
           a.y = _PosY;  
           b.x = _ImageWidthScaled +_PosX;
           b.y = _PosY;
           c.x = _ImageWidthScaled +_PosX;
           c.y = _ImageHeightScaled +_PosY;
           d.x = _PosX;
           d.y = _ImageHeightScaled +_PosY;
           //rotacija tacaka i postavljanje poligona
           
           Point center = new Point(_ImageWidthScaled/2+_PosX,_ImageHeightScaled/2+_PosY);
           a.x = (int) (PointAfterRotation(a.x,a.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
           a.y = (int) (PointAfterRotation(a.x,a.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
           b.x = (int) (PointAfterRotation(b.x,b.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
           b.y = (int) (PointAfterRotation(b.x,b.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
           c.x = (int) (PointAfterRotation(c.x,c.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
           c.y = (int) (PointAfterRotation(c.x,c.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
           d.x = (int) (PointAfterRotation(d.x,d.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
           d.y = (int) (PointAfterRotation(d.x,d.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
           
           polygonTemp = Polygon.Builder()
             	    .addVertex(RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[0])
             	    .addVertex(RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[1])
             	    .addVertex((RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[2]))
             	    .addVertex((RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[3]))
             	    .build();
      }
    }

    public StickerImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickerImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        _ScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        _RotateDetector = new RotateGestureDetector(new RotateListener(_Listener));
        
    }
    
    private void init(Context context) {
    	_ScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        _BorderLeftLine = new Paint();
        _BorderRightLine = new Paint();
        _BorderBottomLine = new Paint();
        _BorderTopLine = new Paint();
        _TestLine =  new Paint();
        setBorderParams(Color.RED,2);
 
    }
    
    private void setBorderParams(int color, float width) {
        _BorderLeftLine.setColor(color);
        _BorderLeftLine.setStrokeWidth(width);
        _BorderRightLine.setColor(color);
        _BorderRightLine.setStrokeWidth(width);
        _BorderBottomLine.setColor(color);
        _BorderBottomLine.setStrokeWidth(width);
        _BorderTopLine.setColor(color);
        _TestLine.setColor(Color.YELLOW);
        _BorderTopLine.setStrokeWidth(width);
        _TestLine .setStrokeWidth(width);
        _Image.setBounds(0, 0,_Image.getIntrinsicWidth(),_Image.getIntrinsicHeight());
 
    }
    
    private void removeBorderParams() {
        _BorderLeftLine.setColor(Color.TRANSPARENT);
        _BorderLeftLine.setStrokeWidth(0);
        _BorderRightLine.setColor(Color.TRANSPARENT);
        _BorderRightLine.setStrokeWidth(0);
        _BorderBottomLine.setColor(Color.TRANSPARENT);
        _BorderBottomLine.setStrokeWidth(0);
        _BorderTopLine.setColor(Color.TRANSPARENT);
        _BorderTopLine.setStrokeWidth(0);
        _Image.setBounds(0, 0,_Image.getIntrinsicWidth(),_Image.getIntrinsicHeight());
 
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {


        try
        {
            // Let the ScaleGestureDetector inspect all events.
            _ScaleDetector.onTouchEvent(ev);
            _RotateDetector.onTouchEvent(ev);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        boolean intercept = false;
        
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: {
        	
        	
            final float x = ev.getX();
            final float y = ev.getY();

            mLastTouchX = x;
            mLastTouchY = y;
            mActivePointerId = ev.getPointerId(0);
            
            Point p = new Point((int)mLastTouchX,(int)mLastTouchY);
            if(polygonTemp.contains(p))
            {
            		 Log.i("TAG","My view is here: "+_NumberView);
            		 intercept = true;
            		 _Selected = true;
                AddWallpaperFragment._IdOfSelectedView = _NumberView;
            		 mStyle.setmCurrentView(_NumberView);
                AddWallpaperFragment.invalidateOtherStickers(_NumberView);
            		 invalidate();
            }
            else  
            {
                AddWallpaperFragment._IdOfSelectedView = -1;
            		_Selected = false;
            		invalidate();
            }
            break;
        }

        case MotionEvent.ACTION_MOVE: {
            final int pointerIndex = ev.findPointerIndex(mActivePointerId);
            final float x = ev.getX(pointerIndex);
            final float y = ev.getY(pointerIndex);

            // Only move if the ScaleGestureDetector isn't processing a gesture.
            if (!_ScaleDetector.isInProgress()) {
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                _PosX += dx;
                _PosY += dy;
  
                invalidate();
            }

            mLastTouchX = x;
            mLastTouchY = y;

            break;
        }

        case MotionEvent.ACTION_UP: {
        
        	setFocusable(false);
        	 _ImageWidthScaled = (int) (_ImageWidth*_ScaleFactor);
        	 _ImageHeightScaled = (int) (_ImageHeight*_ScaleFactor);
        	 _Selected = false;
        	
             mActivePointerId = INVALID_POINTER_ID;

             a.x = _PosX;
             a.y = _PosY;  
             b.x = _ImageWidthScaled +_PosX;
             b.y = _PosY;
             c.x = _ImageWidthScaled +_PosX;
             c.y = _ImageHeightScaled +_PosY;
             d.x = _PosX;
             d.y = _ImageHeightScaled +_PosY;
             //rotacija tacaka i postavljanje poligona
             
             Point center = new Point(_ImageWidthScaled/2+_PosX,_ImageHeightScaled/2+_PosY);
             a.x = (int) (PointAfterRotation(a.x,a.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
             a.y = (int) (PointAfterRotation(a.x,a.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
             b.x = (int) (PointAfterRotation(b.x,b.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
             b.y = (int) (PointAfterRotation(b.x,b.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
             c.x = (int) (PointAfterRotation(c.x,c.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
             c.y = (int) (PointAfterRotation(c.x,c.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
             d.x = (int) (PointAfterRotation(d.x,d.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
             d.y = (int) (PointAfterRotation(d.x,d.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
             
             polygonTemp = Polygon.Builder()
               	    .addVertex(RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[0])
               	    .addVertex(RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[1])
               	    .addVertex((RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[2]))
               	    .addVertex((RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[3]))
               	    .build();
             break;
        }

        case MotionEvent.ACTION_CANCEL: {
            mActivePointerId = INVALID_POINTER_ID;
            break;
        }

        case MotionEvent.ACTION_POINTER_UP: {
            final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                    >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            final int pointerId = ev.getPointerId(pointerIndex);
            if (pointerId == mActivePointerId) {
                // This was our active pointer going up. Choose a new
                // active pointer and adjust accordingly.
                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                mLastTouchX = ev.getX(newPointerIndex);
                mLastTouchY = ev.getY(newPointerIndex);
                mActivePointerId = ev.getPointerId(newPointerIndex);
              
            }
            break;
        }
        }

        return intercept;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
      
        if(_Flipped)
        {
        	canvas.translate(_PosX, _PosY);
        	canvas.scale(-_ScaleFactor, _ScaleFactor);
        	reflectionTouchPolygon();
        	canvas.rotate(-_LastAngle,-_Image.getIntrinsicWidth()/2,_Image.getIntrinsicHeight()/2);
        	canvas.translate(-_Image.getIntrinsicWidth(),0);
        	
        }
        else 
        {
        	canvas.translate(_PosX, _PosY);
        	canvas.scale(_ScaleFactor, _ScaleFactor);//,_Image.getIntrinsicWidth()/2,_Image.getIntrinsicHeight()/2);
        	canvas.rotate(_LastAngle,_Image.getIntrinsicWidth()/2,_Image.getIntrinsicHeight()/2);
        	reflectionRestoreTouchPolygon();
        }
        
     
        if (_Selected){
            canvas.drawLine(0,
                    0,
                    _Image.getIntrinsicWidth(),
                    0,
                    _BorderTopLine);
            canvas.drawLine(0, _Image.getIntrinsicHeight(),
                    _Image.getIntrinsicWidth(),
                    _Image.getIntrinsicHeight(),
                    _BorderBottomLine);
            canvas.drawLine(0,
                    0,
                    0, 
                    _Image.getIntrinsicHeight(),
                    _BorderLeftLine);
            canvas.drawLine(_Image.getIntrinsicWidth(),
                    0,
                    _Image.getIntrinsicWidth(),
                    _Image.getIntrinsicHeight(),
                    _BorderRightLine);
            
            _TestLine.setStyle(Paint.Style.STROKE);
        }
        _Image.draw(canvas);
        canvas.restore();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
        	_ScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
        	_ScaleFactor = Math.max(/*0.30f*/min_size, Math.min(_ScaleFactor, max_size));
            invalidate();
            return true;
        }
    }
    
    private class RotateListener extends RotateGestureDetector  implements RotateGestureDetector.OnRotationGestureListener{
    	
  		public RotateListener(OnRotationGestureListener listener) {
  			super(listener);
  			// TODO Auto-generated constructor stub
  		}

  		@Override
  		public boolean OnRotation(RotateGestureDetector rotationDetector) {
  			_LastAngle -= rotationDetector.getAngle();
  			return false;
  		}
  	}
    
   boolean IsPiontBellow(int x, int y, int k, int n)
   {
	   if(y<k*x+n)
		   return true;
	   else return false;
   }


  Point PointAfterRotation(float x, float y, double angleInRadians, float centerx, float centery) //position of point after rotatio0n around point center
   {
	   Point a1 = new Point(0,0);
	   a1.x = (int) (centerx + Math.cos(angleInRadians)*(x-centerx) - Math.sin(angleInRadians)*(y-centery));
	   a1.y = (int) (centery  + Math.sin(angleInRadians)*(x-centerx) + Math.cos(angleInRadians)*(y-centery));
	   return a1;
   }
  
  Point[] RotatedPolygon(Polygon poly, Point center, double angle)
  {
	  Point[] vertices = new Point[4];
	  for(int i = 0; i < vertices.length; i++) {
		  vertices[i] = new Point(0,0);
		}
	  //teme A
	  vertices[0].x = (float) (center.x + (_ImageWidthScaled/2) * Math.cos(angle) - (_ImageHeightScaled/2) * Math.sin(angle));
	  vertices[0].y = (float) (center.y + ( _ImageHeightScaled / 2 ) * Math.cos(angle) + ( _ImageWidthScaled / 2 ) * Math.sin(angle));
	  vertices[1].x = (float) (center.x - (_ImageWidthScaled/2) * Math.cos(angle) -  (_ImageHeightScaled/2) * Math.sin(angle));
	  vertices[1].y = (float) (center.y + ( _ImageHeightScaled / 2 ) * Math.cos(angle)  - ( _ImageWidthScaled / 2 ) * Math.sin(angle));
	  vertices[2].x = (float) ( center.x - ( _ImageWidthScaled / 2 ) * Math.cos(angle) + ( _ImageHeightScaled / 2 ) * Math.sin(angle));
	  vertices[2].y = (float) (center.y - ( _ImageHeightScaled / 2 ) * Math.cos(angle)  - ( _ImageWidthScaled / 2 ) * Math.sin(angle));
	  vertices[3].x = (float) (center.x + ( _ImageWidthScaled / 2 ) * Math.cos(angle) + ( _ImageHeightScaled / 2 ) * Math.sin(angle));
	  vertices[3].y = (float) (center.y - ( _ImageHeightScaled / 2 ) * Math.cos(angle) + ( _ImageWidthScaled / 2 ) *  Math.sin(angle));
	  return vertices;
  }

   double DegreesToRadians(float degrees)
   {
	   return degrees* Math.PI/180;
   }
   
   public int get_NumberView() {
		return _NumberView;
	}

	public void set_NumberView(int _NumberView) {
		this._NumberView = _NumberView;
	}
	
	public void setFlipped(boolean  f) {
		this._Flipped = f;
	}
	
	public boolean getFlipped()
	{
		return this._Flipped;
	}
	
	public void set_Selected(boolean s)
	{
		_Selected = s;
	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
  	 setMeasuredDimension((int)(this.mStyle.canvasWidth), (int)(this.mStyle.canvasHeight));
    }
    
    public void reflectionTouchPolygon()
    {
    	 a.x = _PosX-_ImageWidthScaled;
         a.y = _PosY;  
         b.x = _ImageWidthScaled +_PosX-_ImageWidthScaled;
         b.y = _PosY;
         c.x = _ImageWidthScaled +_PosX-_ImageWidthScaled;
         c.y = _ImageHeightScaled +_PosY;
         d.x = _PosX-_ImageWidthScaled;
         d.y = _ImageHeightScaled +_PosY;
         //rotacija tacaka i postavljanje poligona
         
         Point center = new Point(_ImageWidthScaled/2+_PosX,_ImageHeightScaled/2+_PosY);
         a.x = (int) (PointAfterRotation(a.x,a.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
         a.y = (int) (PointAfterRotation(a.x,a.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
         b.x = (int) (PointAfterRotation(b.x,b.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
         b.y = (int) (PointAfterRotation(b.x,b.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
         c.x = (int) (PointAfterRotation(c.x,c.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
         c.y = (int) (PointAfterRotation(c.x,c.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
         d.x = (int) (PointAfterRotation(d.x,d.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
         d.y = (int) (PointAfterRotation(d.x,d.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
         
         polygonTemp = Polygon.Builder()
           	    .addVertex(RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[0])
           	    .addVertex(RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[1])
           	    .addVertex((RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[2]))
           	    .addVertex((RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[3]))
           	    .build();
    }

    public void reflectionRestoreTouchPolygon()
    {
    	 a.x = _PosX;
         a.y = _PosY;  
         b.x = _ImageWidthScaled +_PosX;
         b.y = _PosY;
         c.x = _ImageWidthScaled +_PosX;
         c.y = _ImageHeightScaled +_PosY;
         d.x = _PosX;
         d.y = _ImageHeightScaled +_PosY;
         //rotacija tacaka i postavljanje poligona
         
         Point center = new Point(_ImageWidthScaled/2+_PosX,_ImageHeightScaled/2+_PosY);
         a.x = (int) (PointAfterRotation(a.x,a.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
         a.y = (int) (PointAfterRotation(a.x,a.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
         b.x = (int) (PointAfterRotation(b.x,b.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
         b.y = (int) (PointAfterRotation(b.x,b.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
         c.x = (int) (PointAfterRotation(c.x,c.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
         c.y = (int) (PointAfterRotation(c.x,c.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
         d.x = (int) (PointAfterRotation(d.x,d.y,DegreesToRadians(_LastAngle),center.x, center.y)).x;
         d.y = (int) (PointAfterRotation(d.x,d.y,DegreesToRadians(_LastAngle),center.x, center.y)).y;
         
         polygonTemp = Polygon.Builder()
           	    .addVertex(RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[0])
           	    .addVertex(RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[1])
           	    .addVertex((RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[2]))
           	    .addVertex((RotatedPolygon(polygon,center,DegreesToRadians(_LastAngle))[3]))
           	    .build();
    }
}