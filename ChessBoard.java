package com.webrtc.look;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChessBoard extends View {

    private static final String TAG = "ChessBoard";
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float grid;
    private float margin;
    private float lineWitdh = 4;
    private List<Piece> currentPieces = new ArrayList<>();
    private List<String> phaces = new ArrayList<>();
    private int step = Action.IDLE;
    private boolean redMove = true;
    private Point selectPoint;
    private Piece selectPiece;

    private Gson gson = new Gson();

    public @interface Type {

        String 车 = "车";
        String 马 = "马";
        String 炮 = "炮";
        String 相 = "相";
        String 士 = "士";
        String 将 = "将";
        String 兵 = "兵";
    }

    public @interface Action {

        int IDLE = 0;
        int SELECT = 1;
    }

    private static class Piece {

        @Type String type;
        int x;
        int y;
        boolean red;

        public Piece(String type, int x, int y, boolean red) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.red = red;
        }

        public boolean sameLoc(int x, int y) {
            return this.x == x && this.y == y;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("Piece{");
            sb.append("type:'").append(type).append('\'');
            sb.append(", x:").append(x);
            sb.append(", y:").append(y);
            sb.append(", red:").append(red);
            sb.append('}');
            return sb.toString();
        }
    }

    public ChessBoard(Context context) {
        this(context, null, 0);
    }

    public ChessBoard(Context context, AttributeSet set) {
        this(context, set, 0);
    }

    public ChessBoard(Context context, AttributeSet set, int defStyleAttr) {
        super(context, set, defStyleAttr);
        init();

    }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        textPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float width = Math.min(getMeasuredWidth(), getMeasuredHeight() / 10f * 9f);
        grid = width / 9f;
        margin = grid / 2f;
        setMeasuredDimension((int) width, (int) (width / 9f * 10));

        lineWitdh = width / 320f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        paint.setColor(Color.parseColor("#F4A460"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, height, paint);

        paint.setColor(getResources().getColor(R.color.black));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWitdh);
        canvas.drawRect(margin, margin, width - margin, height - margin, paint);
        paint.setStrokeWidth(lineWitdh * 2);
        float frame = margin * .8f;
        canvas.drawRect(frame, frame, width - frame, height - frame, paint);

        paint.setStrokeWidth(lineWitdh);
        for (int i = 1; i <= 8; i++) {
            float lineY = grid * i + margin;
            canvas.drawLine(margin, lineY, width - margin, lineY, paint);
        }

        for (int i = 1; i <= 8; i++) {
            float lineX = grid * i + margin;
            canvas.drawLine(lineX, margin, lineX, margin + grid * 4, paint);
            canvas.drawLine(lineX, height - margin, lineX, height - margin - grid * 4, paint);
        }

        float centerYOfTian = grid / 2f * 3f;
        drawCross(canvas, width / 2f, centerYOfTian, grid);
        drawCross(canvas, width / 2f, height - centerYOfTian, grid);

        float crossY = margin + grid * 2;
        drawSolderCross(canvas, margin + grid, crossY, true, true);
        drawSolderCross(canvas, margin + grid * 7, crossY, true, true);

        crossY = margin + grid * 7;
        drawSolderCross(canvas, margin + grid, crossY, true, true);
        drawSolderCross(canvas, margin + grid * 7, crossY, true, true);

        crossY = margin + grid * 3;
        drawSolderCross(canvas, margin, crossY, false, true);
        drawSolderCross(canvas, margin + grid * 2, crossY, true, true);
        drawSolderCross(canvas, margin + grid * 4, crossY, true, true);
        drawSolderCross(canvas, margin + grid * 6, crossY, true, true);
        drawSolderCross(canvas, margin + grid * 8, crossY, true, false);

        crossY = margin + grid * 6;
        drawSolderCross(canvas, margin, crossY, false, true);
        drawSolderCross(canvas, margin + grid * 2, crossY, true, true);
        drawSolderCross(canvas, margin + grid * 4, crossY, true, true);
        drawSolderCross(canvas, margin + grid * 6, crossY, true, true);
        drawSolderCross(canvas, margin + grid * 8, crossY, true, false);

        textPaint.setColor(getResources().getColor(R.color.black));
        textPaint.setTextSize(grid * .6f);
        drawHorizontalText(canvas, "楚", margin + grid * 1, margin + grid * 5, grid, true);
        drawHorizontalText(canvas, "河", margin + grid * 3, margin + grid * 5, grid, true);
        drawHorizontalText(canvas, "汉", margin + grid * 5, margin + grid * 4, grid, false);
        drawHorizontalText(canvas, "界", margin + grid * 7, margin + grid * 4, grid, false);

        textPaint.setTextSize(grid * .4f);
        for (Piece piece : currentPieces) {
            float centerX = margin + piece.x * grid;
            float centerY = margin + piece.y * grid;

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(getResources().getColor(R.color.black));
            canvas.drawCircle(centerX, centerY, grid * .4f, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#BDB76B"));
            canvas.drawCircle(centerX, centerY, grid * .4f, paint);

            textPaint.setColor(piece.red ? Color.parseColor("#8B0000") : Color.BLACK);
            textPaint.setFakeBoldText(true);
            canvas.drawText(piece.type, centerX - grid * .2f, centerY + grid * .15f, textPaint);
        }

        if (selectPoint != null) {
            paint.setStrokeWidth(lineWitdh);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            float centerX = margin + selectPoint.x * grid;
            float centerY = margin + selectPoint.y * grid;
            float offset = grid * .4f;
            canvas.drawRect(centerX - offset, centerY - offset, centerX + offset, centerY + offset, paint);
        }
    }

    private void drawCross(Canvas canvas, float centerX, float centerY, float size) {
        canvas.drawLine(centerX, centerY, centerX + size, centerY + size, paint);
        canvas.drawLine(centerX, centerY, centerX - size, centerY + size, paint);
        canvas.drawLine(centerX, centerY, centerX + size, centerY - size, paint);
        canvas.drawLine(centerX, centerY, centerX - size, centerY - size, paint);
    }

    private void drawHorizontalText(Canvas canvas, String text, float startX, float startY, float size, boolean up) {
        Path path = new Path();
        path.moveTo(startX, startY + (up ? -size * .2f : size * .2f));
        path.lineTo(startX, startY + (up ? -size : size));
        canvas.drawTextOnPath(text, path, 0, 0, textPaint);
    }

    private void drawSolderCross(Canvas canvas, float centerX, float centerY, boolean left, boolean rght) {
        float offset = grid / 10f;
        float length = offset * 2;
        float x, y;

        if (left) {
            x = centerX - offset;
            y = centerY - offset;
            canvas.drawLine(x, y, x, y - length, paint);
            canvas.drawLine(x, y, x - length, y, paint);

            x = centerX - offset;
            y = centerY + offset;
            canvas.drawLine(x, y, x, y + length, paint);
            canvas.drawLine(x, y, x - length, y, paint);
        }

        if (rght) {
            x = centerX + offset;
            y = centerY - offset;
            canvas.drawLine(x, y, x, y - length, paint);
            canvas.drawLine(x, y, x + length, y, paint);

            x = centerX + offset;
            y = centerY + offset;
            canvas.drawLine(x, y, x, y + length, paint);
            canvas.drawLine(x, y, x + length, y, paint);
        }
    }

    public void start() {
        redMove = true;
        currentPieces.clear();
        phaces.clear();
        for (int i = 0; i < 9; i += 2) {
            Piece piece = new Piece(Type.兵, i, 6, true);
            currentPieces.add(piece);
            Piece piece2 = new Piece(Type.兵, i, 3, false);
            currentPieces.add(piece2);
        }

        currentPieces.add(new Piece(Type.炮, 1, 7, true));
        currentPieces.add(new Piece(Type.炮, 7, 7, true));

        currentPieces.add(new Piece(Type.炮, 1, 2, false));
        currentPieces.add(new Piece(Type.炮, 7, 2, false));

        currentPieces.add(new Piece(Type.车, 0, 0, false));
        currentPieces.add(new Piece(Type.车, 8, 0, false));
        currentPieces.add(new Piece(Type.车, 0, 9, true));
        currentPieces.add(new Piece(Type.车, 8, 9, true));

        currentPieces.add(new Piece(Type.马, 1, 0, false));
        currentPieces.add(new Piece(Type.马, 7, 0, false));
        currentPieces.add(new Piece(Type.马, 1, 9, true));
        currentPieces.add(new Piece(Type.马, 7, 9, true));

        currentPieces.add(new Piece(Type.相, 2, 0, false));
        currentPieces.add(new Piece(Type.相, 6, 0, false));
        currentPieces.add(new Piece(Type.相, 2, 9, true));
        currentPieces.add(new Piece(Type.相, 6, 9, true));

        currentPieces.add(new Piece(Type.士, 3, 0, false));
        currentPieces.add(new Piece(Type.士, 5, 0, false));
        currentPieces.add(new Piece(Type.士, 3, 9, true));
        currentPieces.add(new Piece(Type.士, 5, 9, true));

        currentPieces.add(new Piece(Type.将, 4, 0, false));
        currentPieces.add(new Piece(Type.将, 4, 9, true));
        invalidate();
    }

    public void regret() {
        if (!phaces.isEmpty()) {
            currentPieces = fromJsonArray(phaces.remove(phaces.size() - 1), Piece[].class);
            redMove = !redMove;
            invalidate();
        }
    }

    public boolean legalMove(Piece piece, int x, int y) {
        if (piece.sameLoc(x, y)) {
            return false;
        }
        Piece target = findPiece(x, y);
        if (target != null && target.red == piece.red) {
            return false;
        }
        switch (piece.type) {
            case Type.马:
                if (Math.abs(x - piece.x) == 2 && Math.abs(y - piece.y) == 1) {
                    boolean left = x - piece.x < 0;
                    return findPiece(piece.x + (left ? -1 : 1), piece.y) == null && (findPiece(x, y) == null
                            || findPiece(x, y).red ^ piece.red);
                } else if (Math.abs(x - piece.x) == 1 && Math.abs(y - piece.y) == 2) {
                    boolean up = y - piece.y < 0;
                    return findPiece(piece.x, piece.y + (up ? -1 : 1)) == null && (findPiece(x, y) == null
                            || findPiece(x, y).red ^ piece.red);
                }
                return false;
            case Type.兵:
                if (piece.red) {
                    return (piece.y < 5 && piece.y == y && Math.abs(piece.x - x) == 1)
                            || piece.x == x && piece.y == y + 1;
                } else {
                    return (piece.y > 4 && piece.y == y && Math.abs(piece.x - x) == 1)
                            || piece.x == x && piece.y == y - 1;
                }
            case Type.炮:
                if (piece.x == x) {
                    boolean larger = y > piece.y;
                    Piece next = findNextPiece(false, larger, piece);
                    if (next == null || (!next.sameLoc(x, y) && larger == next.y > y)) {
                        return true;
                    } else {
                        Piece nextNext = findNextPiece(false, larger, next);
                        return nextNext.red ^ piece.red && nextNext.sameLoc(x, y);
                    }
                } else if (piece.y == y) {
                    boolean larger = x > piece.x;
                    Piece next = findNextPiece(true, larger, piece);
                    if (next == null || (!next.sameLoc(x, y) && larger == next.x > x)) {
                        return true;
                    } else {
                        Piece nextNext = findNextPiece(true, larger, next);
                        return nextNext.red ^ piece.red && nextNext.sameLoc(x, y);
                    }
                }
                return false;
            case Type.车:
                if (piece.x == x) {
                    boolean larger = y > piece.y;
                    Piece next = findNextPiece(false, larger, piece);
                    if (next == null) {
                        return true;
                    } else if (next.sameLoc(x, y)) {
                        return next.red ^ piece.red;
                    } else {
                        return larger == next.y > y;
                    }
                } else if (piece.y == y) {
                    boolean larger = x > piece.x;
                    Piece next = findNextPiece(true, larger, piece);
                    if (next == null) {
                        return true;
                    } else if (next.sameLoc(x, y)) {
                        return next.red ^ piece.red;
                    } else {
                        return larger == next.x > x;
                    }
                }
                return false;
            case Type.相:
                if (Math.abs(piece.x - x) != 2 || Math.abs(piece.x - x) != 2) {
                    return false;
                } else if (piece.red && y < 5 || !piece.red && y > 4) {
                    return false;
                } else {
                    Piece n = findPiece((piece.x + x) / 2, (piece.y + y) / 2);
                    return n == null;
                }
            case Type.士:
                if (x < 3 || x > 5) {
                    return false;
                } else {
                    if (piece.red && (y < 7) || !piece.red && (y > 2)) {
                        return false;
                    } else {
                        return Math.abs(piece.x - x) == 1 && Math.abs(piece.y - y) == 1;
                    }
                }
            case Type.将:
                if (x < 3 || x > 5) {
                    return false;
                } else {
                    if (piece.red && (y < 7) || !piece.red && (y > 2)) {
                        return false;
                    } else {
                        return Math.abs(piece.x - x) + Math.abs(piece.y - y) == 1;
                    }
                }
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) (event.getX() - margin);
            int y = (int) (event.getY() - margin);

            x = (x + (int) grid / 2) / (int) grid;
            y = (y + (int) grid / 2) / (int) grid;
            Log.i(TAG, "onTouchEvent, measure location:" + event.getX() + "x" + event.getY() + " > " + x + "x" + y);
            Piece piece = findPiece(x, y);
            if (step == Action.IDLE) {
                if (piece != null && piece.red == redMove) {
                    selectPoint = new Point(x, y);
                    selectPiece = piece;
                    step = Action.SELECT;
                    invalidate();
                    return true;
                }
            } else if (step == Action.SELECT) {
                if (selectPiece != null && legalMove(selectPiece, x, y)) {
                    phaces.add(gson.toJson(currentPieces));
                    if (piece != null) {
                        currentPieces.remove(piece);
                    }
                    selectPoint = null;
                    selectPiece.x = x;
                    selectPiece.y = y;
                    selectPiece = null;
                    step = Action.IDLE;
                    redMove = !redMove;
                    invalidate();
                    return true;
                } else if (piece != null) {
                    selectPoint = new Point(x, y);
                    selectPiece = piece;
                    invalidate();
                    return true;
                } else {
                    selectPiece = null;
                    selectPoint = null;
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);

    }

    private Piece findPiece(int x, int y) {
        for (Piece piece : currentPieces) {
            if (piece.sameLoc(x, y)) {
                return piece;
            }
        }
        return null;
    }

    private Piece findNextPiece(boolean isX, boolean larger, Piece piece) {
        Piece next = null;
        for (Piece p : currentPieces) {
            if (p == piece) {
                continue;
            }
            if (isX && p.y == piece.y) {
                if (larger != p.x > piece.x) {
                    continue;
                }
                if (next == null) {
                    next = p;
                } else {
                    if (larger == next.x > p.x) {
                        next = p;
                    }
                }
            } else if (!isX && p.x == piece.x) {
                if (larger != p.y > piece.y) {
                    continue;
                }
                if (next == null) {
                    next = p;
                } else {
                    if (larger == next.y > p.y) {
                        next = p;
                    }
                }
            }
        }
        Log.i(TAG, "findNextPiece, x:" + isX + ", larger:" + larger + ", p:" + piece + ", next:" + next);
        return next;
    }

    private <T> List<T> fromJsonArray(String jsonString, Class<T[]> cls) {
        T[] array =  gson.fromJson(jsonString, cls);
        if (array == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(array));
    }
}
