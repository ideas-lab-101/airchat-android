package com.android.crypt.chatapp.widget.swipexlistview;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.utility.Common.DensityUtil;
import com.orhanobut.logger.Logger;

public class SwipeXListView extends ListView implements OnScrollListener{

	private Boolean mIsHorizontal;
	private View mPreItemView;
	private View mCurrentItemView;
	private float mFirstX;
	private float mFirstY;
	private int mRightViewWidth;
	private final int mDuration = 100;
	private final int mDurationStep = 10;
	private boolean mIsShown;
	private float mLastY = -1; // save event y
	private Scroller mScroller; // used for scroll back
	private OnScrollListener mScrollListener; // user's scroll listener

	// the interface to trigger refresh and load more.
	private IXListViewListener mListViewListener;
	// -- header view
	private SwipeXListViewHeader mHeaderView;
	// header view content, use it to calculate the Header's height. And hide it
	// when disable pull refresh.
	private RelativeLayout mHeaderViewContent;
	private TextView mHeaderTimeView;
	private int mHeaderViewHeight; // header view's height
	private boolean mEnablePullRefresh = true;
	private boolean mPullRefreshing = false; // is refreashing.
	// -- footer view
	private SwipeXListViewFooter mFooterView;
	private boolean mEnablePullLoad;
	private boolean mPullLoading;
	private boolean mIsFooterReady = false;
	// total list items, used to detect is at the bottom of listview.
	private int mTotalItemCount;
	// for mScroller, scroll back from header or footer.
	private int mScrollBack;
	private final static int SCROLLBACK_HEADER = 0;
	private final static int SCROLLBACK_FOOTER = 1;
	private final static int SCROLL_DURATION = 400; // scroll back duration
	private final static int PULL_LOAD_MORE_DELTA = 50; // when pull up >= 50px
	// at bottom, trigger
	// load more.
	private final static float OFFSET_RADIO = 1.8f; // support iOS like pull
	// feature.

	public SwipeXListView(Context context) {
		this(context, null);
		initWithContext(context);
	}

	public SwipeXListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		initWithContext(context);
	}

	public SwipeXListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// 获取默认的左滑划开宽度
		//TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeListViewStyle);
		mRightViewWidth = context.getResources().getDimensionPixelSize(R.dimen.swipe_menu_width);
		//(int) mTypedArray.getDimension(R.styleable.swipelistviewstyle_right_width, 200);
		//mRightViewWidth = (int) mTypedArray.getDimension(R.styleable.SwipeListViewStyle_right_width, 160);
		//mTypedArray.recycle();
		initWithContext(context);
	}



	private void initWithContext(Context context) {
		mScroller = new Scroller(context, new DecelerateInterpolator());
		// XListView need the scroll event, and it will dispatch the event to
		// user's listener (as a proxy).
		super.setOnScrollListener(this);

		// init header view
		mHeaderView = new SwipeXListViewHeader(context);
		mHeaderViewContent = (RelativeLayout) mHeaderView
				.findViewById(R.id.xlistview_header_content);
		mHeaderTimeView = (TextView) mHeaderView
				.findViewById(R.id.xlistview_header_time);
		addHeaderView(mHeaderView);

		// init footer view
		mFooterView = new SwipeXListViewFooter(context);

		// init header height
		mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						mHeaderViewHeight = mHeaderViewContent.getHeight();
						getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
					}
				});
//		mContext = context;
//		initBounceListView();
	}

	public void hideHeadViewLayout(){
		mHeaderTimeView.setVisibility(INVISIBLE);
        mHeaderViewContent.setVisibility(INVISIBLE);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		// make sure XListViewFooter is the last footer view, and only add once.
		if (mIsFooterReady == false) {
			mIsFooterReady = true;
			addFooterView(mFooterView);
		}
		super.setAdapter(adapter);
	}

	/**
	 * enable or disable pull down refresh feature.
	 *
	 * @param enable
	 */
	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) { // disable, hide the content
			mHeaderViewContent.setVisibility(View.INVISIBLE);
		} else {
			mHeaderViewContent.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * enable or disable pull up load more feature.
	 *
	 * @param enable
	 */
	public void setPullLoadEnable(boolean enable) {
		mEnablePullLoad = enable;
		if (!mEnablePullLoad) {
			mFooterView.hide();
			mFooterView.setOnClickListener(null);
		} else {
			mPullLoading = false;
			mFooterView.show();
			mFooterView.setState(SwipeXListViewFooter.STATE_NORMAL);
			// both "pull up" and "click" will invoke load more.
			mFooterView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startLoadMore();
				}
			});
		}
	}

	/**
	 * stop refresh, reset header view.
	 */
	public void stopRefresh() {
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			resetHeaderHeight();
		}
	}

	/**
	 * stop load more, reset footer view.
	 */
	public void stopLoadMore() {
		if (mPullLoading == true) {
			mPullLoading = false;
			mFooterView.setState(SwipeXListViewFooter.STATE_NORMAL);
		}
	}

	/**
	 * set last refresh time
	 *
	 * @param time
	 */
	public void setRefreshTime(String time) {
		mHeaderTimeView.setText(time);
	}

	private void invokeOnScrolling() {
		if (mScrollListener instanceof OnXScrollListener) {
			OnXScrollListener l = (OnXScrollListener) mScrollListener;
			l.onXScrolling(this);
		}
	}

	private void updateHeaderHeight(float delta) {
		mHeaderView.setVisibleHeight((int) delta
				+ mHeaderView.getVisibleHeight());
		if (mEnablePullRefresh && !mPullRefreshing) {
			if (mHeaderView.getVisibleHeight() > mHeaderViewHeight) {
				mHeaderView.setState(SwipeXListViewHeader.STATE_READY);
			} else {
				mHeaderView.setState(SwipeXListViewHeader.STATE_NORMAL);
			}
		}
		setSelection(0); // scroll to top each time
	}

	/**
	 * reset header view's height.
	 */
	private void resetHeaderHeight() {
		int height = mHeaderView.getVisibleHeight();
		if (height == 0) // not visible.
			return;
		// refreshing and header isn't shown fully. do nothing.
		if (mPullRefreshing && height <= mHeaderViewHeight) {
			return;
		}
		int finalHeight = 0; // default: scroll back to dismiss header.
		// is refreshing, just scroll back to show all the header.
		if (mPullRefreshing && height > mHeaderViewHeight) {
			finalHeight = mHeaderViewHeight;
		}
		mScrollBack = SCROLLBACK_HEADER;
		mScroller.startScroll(0, height, 0, finalHeight - height,
				SCROLL_DURATION);
		// trigger computeScroll
		invalidate();
	}

	private void updateFooterHeight(float delta) {
		int height = mFooterView.getBottomMargin() + (int) delta;
		if (mEnablePullLoad && !mPullLoading) {
			if (height > PULL_LOAD_MORE_DELTA) { // height enough to invoke load
				// more.
				mFooterView.setState(SwipeXListViewFooter.STATE_READY);
			} else {
				mFooterView.setState(SwipeXListViewFooter.STATE_NORMAL);
			}
		}
		mFooterView.setBottomMargin(height);

		setSelection(mTotalItemCount - 1); // scroll to bottom
	}

	private void resetFooterHeight() {
		int bottomMargin = mFooterView.getBottomMargin();
		if (bottomMargin > 0) {
			mScrollBack = SCROLLBACK_FOOTER;
			mScroller.startScroll(0, bottomMargin, 0, -bottomMargin,
					SCROLL_DURATION);
			invalidate();
		}
	}

	private void startLoadMore() {
		mPullLoading = true;
		mFooterView.setState(SwipeXListViewFooter.STATE_LOADING);
		if (mListViewListener != null) {
			mListViewListener.onLoadMore();
		}
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (mScrollBack == SCROLLBACK_HEADER) {
				mHeaderView.setVisibleHeight(mScroller.getCurrY());
			} else {
				mFooterView.setBottomMargin(mScroller.getCurrY());
			}
			postInvalidate();
			invokeOnScrolling();
		}
		super.computeScroll();
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
		// send to user's listener
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

	public void setXListViewListener(IXListViewListener l) {
		mListViewListener = l;
	}

	/**
	 * you can listen ListView.OnScrollListener or this one. it will invoke
	 * onXScrolling when header/footer scroll back.
	 */
	public interface OnXScrollListener extends OnScrollListener {
		public void onXScrolling(View view);
	}

	/**
	 * implements this interface to get refresh/load more event.
	 */
	public interface IXListViewListener {
		public void onRefresh();
		public void onLoadMore();
//		public void curOffsetYValue(int value);
	}

	/**
	 * return true, deliver to listView. return false, deliver to child. if
	 * move, return true
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		float lastX = ev.getX();
		float lastY = ev.getY();
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mIsHorizontal = null;
				mFirstX = lastX;
				mFirstY = lastY;
				int motionPosition = pointToPosition((int) mFirstX, (int) mFirstY);

				if (motionPosition >= 0) {
					View currentItemView = getChildAt(motionPosition - getFirstVisiblePosition());
					mPreItemView = mCurrentItemView;
					mCurrentItemView = currentItemView;
				}
//				onInterceptTouchEventActionDown(ev);
				break;

			case MotionEvent.ACTION_MOVE:
				float dx = lastX - mFirstX;
				float dy = lastY - mFirstY;

				if (Math.abs(dx) >= 5 && Math.abs(dy) >= 5) {
					return true;
				}

				break;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				System.out.println("onInterceptTouchEvent----->ACTION_UP");
				if (mIsShown && (mPreItemView != mCurrentItemView || isHitCurItemLeft(lastX))) {
					System.out.println("1---> hiddenRight");
					/**
					 * 情况一：
					 * <p>
					 * 一个Item的右边布局已经显示，
					 * <p>
					 * 这时候点击任意一个item, 那么那个右边布局显示的item隐藏其右边布局
					 */
					hiddenRight(mPreItemView);
				}

				break;
		}

		return super.onInterceptTouchEvent(ev);
	}

	private boolean isHitCurItemLeft(float x) {
		return x < getWidth() - mRightViewWidth;
	}

	/**
	 * @param dx
	 * @param dy
	 * @return judge if can judge scroll direction
	 */
	private boolean judgeScrollDirection(float dx, float dy) {
		boolean canJudge = true;

		if (Math.abs(dx) > 30 && Math.abs(dx) > 2 * Math.abs(dy)) {
			mIsHorizontal = true;
			System.out.println("mIsHorizontal---->" + mIsHorizontal);
		} else if (Math.abs(dy) > 30 && Math.abs(dy) > 2 * Math.abs(dx)) {
			mIsHorizontal = false;
			System.out.println("mIsHorizontal---->" + mIsHorizontal);
		} else {
			canJudge = false;
		}

		return canJudge;
	}

	/**
	 * return false, can't move any direction. return true, cant't move
	 * vertical, can move horizontal. return super.onTouchEvent(ev), can move
	 * both.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		float lastX = ev.getX();
		float lastY = ev.getY();

		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}

		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastY = ev.getRawY();
//				touchDown(ev);
				break;

			case MotionEvent.ACTION_MOVE:
				float dx = lastX - mFirstX;
				float dy = lastY - mFirstY;

				// confirm is scroll direction
				if (mIsHorizontal == null) {
					if (!judgeScrollDirection(dx, dy)) {
						break;
					}
				}

				if (mIsHorizontal) {
					if (mIsShown && mPreItemView != mCurrentItemView) {
						System.out.println("2---> hiddenRight");
						/**
						 * 情况二：
						 * <p>
						 * 一个Item的右边布局已经显示，
						 * <p>
						 * 这时候左右滑动另外一个item,那个右边布局显示的item隐藏其右边布局
						 * <p>
						 * 向左滑动只触发该情况，向右滑动还会触发情况五
						 */
						hiddenRight(mPreItemView);
					}

					if (mIsShown && mPreItemView == mCurrentItemView) {
						dx = dx - mRightViewWidth;
						System.out.println("======dx " + dx);
					}

					// can't move beyond boundary
					if (dx < 0 && dx > -mRightViewWidth) {
						if (null != mCurrentItemView) {
							mCurrentItemView.scrollTo((int) (-dx), 0);
						}
					}

					return true;
				} else {
					if (mIsShown) {
						System.out.println("3---> hiddenRight");
						/**
						 * 情况三：
						 * <p>
						 * 一个Item的右边布局已经显示，
						 * <p>
						 * 这时候上下滚动ListView,那么那个右边布局显示的item隐藏其右边布局
						 */
						hiddenRight(mPreItemView);
					}
//					touchMove(ev);
				}
				final float deltaY = ev.getRawY() - mLastY;
				mLastY = ev.getRawY();
				if (getFirstVisiblePosition() == 0
						&& (mHeaderView.getVisibleHeight() > 0 || deltaY > 0)) {
					// the first item is showing, header has shown or pull down.
					updateHeaderHeight(deltaY / OFFSET_RADIO);
					invokeOnScrolling();
				} else if (getLastVisiblePosition() == mTotalItemCount - 1
						&& (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {
					// last item, already pulled up or want to pull up.
					updateFooterHeight(-deltaY / OFFSET_RADIO);
				}

				break;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				System.out.println("============ACTION_UP");
				clearPressedState();
//				touchCancel();
				if (mIsShown) {
					System.out.println("4---> hiddenRight");
					/**
					 * 情况四：
					 * <p>
					 * 一个Item的右边布局已经显示，
					 * <p>
					 * 这时候左右滑动当前一个item,那个右边布局显示的item隐藏其右边布局
					 */
					hiddenRight(mPreItemView);
				}

				if (mIsHorizontal != null && mIsHorizontal) {
					if (null != mCurrentItemView) {
						if (mFirstX - lastX > mRightViewWidth / 2) {
							showRight(mCurrentItemView);
						} else {
							System.out.println("5---> hiddenRight");
							/**
							 * 情况五：
							 * <p>
							 * 向右滑动一个item,且滑动的距离超过了右边View的宽度的一半，隐藏之。
							 */
							hiddenRight(mCurrentItemView);
						}
					}
					return true;
				}

				mLastY = -1; // reset
				if (getFirstVisiblePosition() == 0) {
					// invoke refresh
					if (mEnablePullRefresh
							&& mHeaderView.getVisibleHeight() > mHeaderViewHeight) {
						mPullRefreshing = true;
						mHeaderView.setState(SwipeXListViewHeader.STATE_REFRESHING);
						if (mListViewListener != null) {
							mListViewListener.onRefresh();
						}
					}
					resetHeaderHeight();
				} else if (getLastVisiblePosition() == mTotalItemCount - 1) {
					// invoke load more.
					if (mEnablePullLoad
							&& mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA) {
						startLoadMore();
					}
					resetFooterHeight();
				}

				break;
		}

		return super.onTouchEvent(ev);
	}

	private void clearPressedState() {
		// TODO current item is still has background, issue
		if (null != mCurrentItemView) {
			mCurrentItemView.setPressed(false);
			setPressed(false);
			refreshDrawableState();
		}
		// invalidate();
	}

	private void showRight(View view) {
		System.out.println("=========showRight");

		Message msg = new MoveHandler().obtainMessage();
		msg.obj = view;
		msg.arg1 = view.getScrollX();
		msg.arg2 = mRightViewWidth;
		msg.sendToTarget();

		mIsShown = true;
	}

	private void hiddenRight(View view) {
		System.out.println("=========hiddenRight");
		if (mCurrentItemView == null) {
			return;
		}
		Message msg = new MoveHandler().obtainMessage();//
		msg.obj = view;
		msg.arg1 = view.getScrollX();
		msg.arg2 = 0;

		msg.sendToTarget();

		mIsShown = false;
	}

	public void hiddenRight() {
		System.out.println("=========hiddenRight");
		if (mCurrentItemView == null) {
			return;
		}

		mCurrentItemView.scrollTo(0, 0);

		mIsShown = false;
	}

	/**
	 * show or hide right layout animation
	 */
	@SuppressLint("HandlerLeak")
	class MoveHandler extends Handler {
		int stepX = 0;

		int fromX;

		int toX;

		View view;

		private boolean mIsInAnimation = false;

		private void animatioOver() {
			mIsInAnimation = false;
			stepX = 0;
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (stepX == 0) {
				if (mIsInAnimation) {
					return;
				}
				mIsInAnimation = true;
				view = (View) msg.obj;
				fromX = msg.arg1;
				toX = msg.arg2;
				stepX = (int) ((toX - fromX) * mDurationStep * 1.0 / mDuration);
				if (stepX < 0 && stepX > -1) {
					stepX = -1;
				} else if (stepX > 0 && stepX < 1) {
					stepX = 1;
				}
				if (Math.abs(toX - fromX) < 10) {
					view.scrollTo(toX, 0);
					animatioOver();
					return;
				}
			}

			fromX += stepX;
			boolean isLastStep = (stepX > 0 && fromX > toX)
					|| (stepX < 0 && fromX < toX);
			if (isLastStep) {
				fromX = toX;
			}

			view.scrollTo(fromX, 0);
			invalidate();

			if (!isLastStep) {
				this.sendEmptyMessageDelayed(0, mDurationStep);
			} else {
				animatioOver();
			}
		}
	}

	public int getRightViewWidth() {
		return mRightViewWidth;
	}

	public void setRightViewWidth(int mRightViewWidth) {
		this.mRightViewWidth = mRightViewWidth;
	}


	/***
	 *
	 *   滑动弹性代码
	 *
	 *
	 */
//	private static final int MAX_Y_OVER_SCROLL_DISTANCE = 100;
//	private Context mContext;
//	private int mMaxYOverScrollDistance;
//	private boolean mStartCalc = false;
//	private int mScrollY = 0;
//	private int mLastMotionY = 0;
//	private int mDeltaY = 0;
//	private int allDeltaY = 0;
//
//	private boolean mIsAnimationRunning = false;
//	private boolean mIsActionUp = false;
//
//	private void initBounceListView(){
//		mMaxYOverScrollDistance = DensityUtil.dip2px(mContext, MAX_Y_OVER_SCROLL_DISTANCE);
//	}

//	public void scrollTo(int x, int y) {
//		super.scrollTo(x, y);
//		mScrollY = y;
//	}

//	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
//								  boolean clampedY) {
//		if(mDeltaY == 0 || mIsActionUp) {
//			return;
//		}

//		scrollBy(0, mDeltaY/2);
//		allDeltaY = allDeltaY + mDeltaY/2;
//		if (mListViewListener != null) {
//			mListViewListener.curOffsetYValue(allDeltaY);
//		}
//	}

//	private void startBoundAnimate() {
//		mIsAnimationRunning = true;
//		final int scrollY = mScrollY;
//		int time = Math.abs(100*scrollY/mMaxYOverScrollDistance);
//		ValueAnimator animator = ValueAnimator.ofInt(0,1).setDuration(time);//设置为动态时间
//		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//			@Override
//			public void onAnimationUpdate(ValueAnimator animator) {
//				float fraction = animator.getAnimatedFraction();
//				scrollTo(0, scrollY - (int) (scrollY * fraction));
//
//				if((int)fraction == 1) {
//					scrollTo(0, 0);
//					resetStatus();
//				}
//			}
//		});
//		animator.start();
//		allDeltaY = 0;
//		if (mListViewListener != null) {
//			mListViewListener.curOffsetYValue(0);
//		}
//	}

//	private void resetStatus() {
//		mIsAnimationRunning = false;
//		mStartCalc = false;
//	}

//	private void onInterceptTouchEventActionDown(MotionEvent event){
//		mIsActionUp = false;
//		resetStatus();
//		if(getFirstVisiblePosition() == 0 ){
//			mStartCalc = true;
//		}else{
//			mStartCalc = false;
//		}
//		mLastMotionY = (int)event.getY();
//	}

//	private void touchDown(MotionEvent event){
//		mIsActionUp = false;
//		allDeltaY = 0;
//		resetStatus();
//		if(getFirstVisiblePosition() == 0){
//			mStartCalc = true;
//		}else{
//			mStartCalc = false;
//		}
//		mLastMotionY = (int)event.getY();
//	}

//	private void touchMove(MotionEvent event){
//		if(!mStartCalc && (getFirstVisiblePosition() == 0) ){
//			mStartCalc = true;
//		}
//
//		final int y = (int) event.getY();
//		mDeltaY = mLastMotionY - y;
//		mLastMotionY = y;
//		if(Math.abs(mScrollY) >= mMaxYOverScrollDistance) {
//			if(mDeltaY * mScrollY > 0) {
//				mDeltaY = 0;
//			}
//		}
//	}

//	private void touchCancel(){
//		allDeltaY = 0;
//		mIsActionUp = true;
//		startBoundAnimate();
//	}

}
