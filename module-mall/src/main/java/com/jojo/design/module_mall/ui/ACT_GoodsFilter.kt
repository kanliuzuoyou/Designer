package com.jojo.design.module_mall.ui

import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.jojo.design.common_base.BaseAppliction
import com.jojo.design.common_base.adapter.rv.MultiItemTypeAdapter
import com.jojo.design.common_base.config.arouter.ARouterConfig
import com.jojo.design.common_base.config.arouter.ARouterConstants
import com.jojo.design.common_base.dagger.mvp.BaseActivity
import com.jojo.design.common_base.utils.RecyclerviewHelper
import com.jojo.design.common_base.utils.ToastUtils
import com.jojo.design.common_ui.view.MultipleStatusView
import com.jojo.design.common_ui.view.MyPopupWindow
import com.jojo.design.common_ui.view.NoScrollGridView
import com.jojo.design.module_mall.R
import com.jojo.design.module_mall.R.id.*
import com.jojo.design.module_mall.adapter.ADA_ChooseCategory
import com.jojo.design.module_mall.adapter.ADA_FilterPrice
import com.jojo.design.module_mall.adapter.ADA_FilterService
import com.jojo.design.module_mall.adapter.ADA_SearchGoods
import com.jojo.design.module_mall.bean.CategoryBean
import com.jojo.design.module_mall.bean.FilterBean
import com.jojo.design.module_mall.bean.RecordsEntity
import com.jojo.design.module_mall.dagger2.DaggerMallComponent
import com.jojo.design.module_mall.dialog.DIA_Filter
import com.jojo.design.module_mall.helper.PopupFilter
import com.jojo.design.module_mall.mvp.SearchContract
import com.jojo.design.module_mall.mvp.presenter.SearchModel
import com.jojo.design.module_mall.mvp.presenter.SearchPresenter
import com.smart.novel.util.bindView
import com.will.weiyuekotlin.component.ApplicationComponent
import kotlinx.android.synthetic.main.act_goods_filter.*
import kotlinx.android.synthetic.main.common_filter_layout.*

/**
 *    author : JOJO
 *    e-mail : 18510829974@163.com
 *    date   : 2019/1/2 6:10 PM
 *    desc   : 商品搜索、筛选结果页面
 */
@Route(path = ARouterConfig.ACT_GoodsFilter)
class ACT_GoodsFilter : BaseActivity<SearchPresenter, SearchModel>(), SearchContract.View {
    var mAdapter: ADA_SearchGoods? = null
    var mAdapterCategory: ADA_ChooseCategory? = null
    var mAdapterRecommend: ADA_ChooseCategory? = null
    var mRecommendList = ArrayList<CategoryBean>()
    var mCategoryPupWindow: MyPopupWindow? = null
    var mRecommendPupWindow: MyPopupWindow? = null
    var mDiaFilter: DIA_Filter? = null
    var gvDiscount: NoScrollGridView? = null
    var gvPrice: NoScrollGridView? = null
    var mAdapterfilterService: ADA_FilterService? = null
    var mAdapterfilterPrice: ADA_FilterPrice? = null
    //    @BindView(R.id.iv_search) lateinit var ivSearch: ImageView //单模块下开发OK，组件化开发会报编译错误
    private val ivSearch by bindView<ImageView>(R.id.iv_search) //Kotlin下组件化开发时只能使用此种方式，否则会报编译错误。

    override fun getContentViewLayoutId(): Int = R.layout.act_goods_filter

    override fun getLoadingMultipleStatusView(): MultipleStatusView? = null

    override fun initDaggerInject(mApplicationComponent: ApplicationComponent) {
        DaggerMallComponent.builder().applicationComponent(BaseAppliction.mApplicationComponent).build().inject(this)
    }

    override fun startEvents() {
        ivSearch.visibility = View.VISIBLE
        var outCategoryId = intent.extras.getString(ARouterConstants.TAGCATEGORY_ID)
        val keyword = intent.extras.getString(ARouterConstants.SEARCH_KEYWORDS)
        setHeaderTitle(keyword)


        mAdapter = ADA_SearchGoods(mContext)
        RecyclerviewHelper.initLayoutManagerRecyclerView(lrecyclerview, mAdapter!!, GridLayoutManager(mContext, 2), mContext)
        // //设置item之间的间距
        lrecyclerview.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView?) {
                //设计图item之间的间距为40 (header占了一个位置，故从位置1开始显示实际的item)
                if (itemPosition != 0) {
                    outRect.top = 40
                }
                if ((itemPosition - 1) % 2 == 0) {
                    outRect.left = 40
                } else {
                    outRect.left = 20
                }
            }
        })
        lrecyclerview.setOnRefreshListener {
            android.os.Handler().postDelayed({ lrecyclerview.refreshComplete(1) }, 2000)
        }

        //传了分类ID，就不传关键字匹配
        if (!TextUtils.isEmpty(outCategoryId)) mPresenter?.getSearchGoods(outCategoryId, "", 0)
        else mPresenter?.getSearchGoods(outCategoryId, keyword, 0)


        mAdapterCategory = ADA_ChooseCategory(mContext)
        mAdapterRecommend = ADA_ChooseCategory(mContext)
        //选择分类弹窗
        mCategoryPupWindow = PopupFilter.initPopupWindow(this, mAdapterCategory!!, true)
        //推荐弹窗
        mRecommendPupWindow = PopupFilter.initPopupWindow(this, mAdapterRecommend!!, true)

        //推荐弹窗数据
        mRecommendList.add(CategoryBean(1, "最新", false))
        mRecommendList.add(CategoryBean(2, "最热", false))
        mRecommendList.add(CategoryBean(3, "推荐", false))
        mAdapterRecommend?.update(mRecommendList, true)
        //选择分类和筛选弹窗的数据
        mPresenter?.getCategoryList(outCategoryId, keyword)
        mPresenter?.getFilterData(outCategoryId)

        //筛选弹窗
        mDiaFilter = DIA_Filter(this)
        gvDiscount = mDiaFilter?.mContentView?.findViewById<NoScrollGridView>(R.id.gv_discount)
        gvPrice = mDiaFilter?.mContentView?.findViewById<NoScrollGridView>(R.id.gv_price)

        mAdapterfilterService = ADA_FilterService(mContext)
        gvDiscount?.adapter = mAdapterfilterService

        mAdapterfilterPrice = ADA_FilterPrice(mContext)
        gvPrice?.adapter = mAdapterfilterPrice

        initListener()
    }

    var isClick = false
    var preBean: CategoryBean? = null
    var preBeanRec: CategoryBean? = null
    private fun initListener() {
        //推荐
        rb_recommend.setOnClickListener {
            rb_category.isSelected = false
            mCategoryPupWindow?.dismiss()

            if (isClick) hideRecommendPopup() else showRecommendPopup()

        }
        //选择分类
        rb_category.setOnClickListener {
            rb_recommend.isSelected = false
            mRecommendPupWindow?.dismiss()

            if (isClick) hideCategoryPopup() else showCategoryPopup()

        }
        //筛选
        rb_filter.setOnClickListener {
            mDiaFilter?.getDialog()?.show()
        }

        mAdapterCategory?.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                var bean = mAdapterCategory!!.dataList[position]
                if (bean == preBean) {
                    bean.isCheck = bean.isCheck
                } else {
                    if (preBean == null) {
                        bean.isCheck = true
                    } else {
                        preBean?.isCheck = false
                        bean.isCheck = true
                    }
                }
                preBean = bean
                mAdapterCategory?.notifyDataSetChanged()

                //隐藏选择分类弹窗
                hideCategoryPopup()
            }

            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
                return false
            }

        })
        mAdapterRecommend?.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                var bean = mAdapterRecommend!!.dataList[position]
                if (bean == preBeanRec) {
                    bean.isCheck = bean.isCheck
                } else {
                    if (preBeanRec == null) {
                        bean.isCheck = true
                    } else {
                        preBeanRec?.isCheck = false
                        bean.isCheck = true
                    }
                }
                preBeanRec = bean
                mAdapterRecommend?.notifyDataSetChanged()
                hideRecommendPopup()
            }

            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
                return false
            }

        })
    }

    /**
     * 隐藏选择分类弹窗
     */
    fun hideCategoryPopup() {
        rb_category.isSelected = false
        isClick = false
        bg_popup.visibility = View.GONE
        mCategoryPupWindow?.dismiss()
    }

    /**
     * 展示选择分类弹窗
     */
    fun showCategoryPopup() {
        rb_category.isSelected = true
        isClick = true
        bg_popup.visibility = View.VISIBLE
        mCategoryPupWindow?.showAsDropDown(ll_filter)
    }

    /**
     * 隐藏推荐弹窗
     */
    fun hideRecommendPopup() {
        rb_recommend.isSelected = false
        isClick = false
        bg_popup.visibility = View.GONE
        mRecommendPupWindow?.dismiss()
    }

    /**
     * 展示推荐弹窗
     */
    fun showRecommendPopup() {
        rb_recommend.isSelected = true
        isClick = true
        bg_popup.visibility = View.VISIBLE
        mRecommendPupWindow?.showAsDropDown(ll_filter)
    }

    override fun getHotList(dataList: List<String>) {
    }


    override fun getSearchGoods(dataBean: RecordsEntity) {
        if (dataBean?.records == null || dataBean.records.isEmpty()) {
            ToastUtils.makeShortToast(BaseAppliction.context.getString(R.string.content_search_content_not_empty))
            return
        }
        mAdapter?.update(dataBean.records, true)
    }

    override fun getCategoryList(dataList: List<CategoryBean>) {
        mAdapterCategory?.update(dataList, true)
    }

    override fun getFilterData(dataBean: FilterBean) {
        mAdapterfilterService?.update(dataBean?.promotionTags, true)
        mAdapterfilterPrice?.update(dataBean?.stageRange, true)

    }
}