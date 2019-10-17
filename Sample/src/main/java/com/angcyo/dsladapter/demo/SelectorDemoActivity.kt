package com.angcyo.dsladapter

import android.graphics.Color
import android.support.v7.widget.GridLayoutManager
import com.angcyo.dsladapter.dsl.DslDemoItem
import kotlin.random.Random.Default.nextInt

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/10/16
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class SelectorDemoActivity : BaseRecyclerActivity() {


    override fun getBaseLayoutId(): Int {
        return R.layout.activity_selector_demo
    }

    //固定选中
    val fixedItemList = mutableListOf<DslAdapterItem>()
    var isSelectorAll = false

    override fun onInitBaseLayoutAfter() {
        super.onInitBaseLayoutAfter()

        dslAdapter.itemSelectorHelper.onItemSelectorListener = object : OnItemSelectorListener {
            override fun onSelectorItemChange(
                selectorItems: MutableList<DslAdapterItem>,
                selectorIndexList: MutableList<Int>,
                isSelectorAll: Boolean,
                selectorParams: SelectorParams
            ) {
                super.onSelectorItemChange(
                    selectorItems,
                    selectorIndexList,
                    isSelectorAll,
                    selectorParams
                )

                this@SelectorDemoActivity.isSelectorAll = isSelectorAll

                dslViewHolder.tv(R.id.tip_view).text = when {
                    isSelectorAll -> "全部选中, 共 ${selectorItems.size} 项"
                    selectorItems.isEmpty() -> "未选中"
                    else -> "选中: ${selectorItems.size} 项"
                }
            }
        }

        dslViewHolder.click(R.id.normal) {
            dslAdapter.itemSelectorHelper.selectorAll(SelectorParams(selector = false))
            dslAdapter.itemSelectorHelper.selectorModel = MODEL_NORMAL
        }
        dslViewHolder.click(R.id.single) {
            dslAdapter.itemSelectorHelper.selectorAll(SelectorParams(selector = false))
            dslAdapter.itemSelectorHelper.selectorModel = MODEL_SINGLE
        }
        dslViewHolder.click(R.id.multi) {
            dslAdapter.itemSelectorHelper.selectorAll(SelectorParams(selector = false))
            dslAdapter.itemSelectorHelper.selectorModel = MODEL_MULTI
        }
        dslViewHolder.click(R.id.all) {
            dslAdapter.itemSelectorHelper.selectorModel = MODEL_MULTI
            dslAdapter.itemSelectorHelper.selectorAll(SelectorParams(selector = !isSelectorAll))
        }

        val spanCount = 4
        val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (!dslAdapter.dslAdapterStatusItem.isNoStatus() ||
                    dslAdapter.getItemData(position)?.itemIsGroupHead == true
                ) {
                    spanCount
                } else {
                    1
                }
            }
        }

        recyclerView.layoutManager = GridLayoutManager(this, spanCount).apply {
            this.spanSizeLookup = spanSizeLookup
        }

        renderAdapter {
            //默认的选择模式
            itemSelectorHelper.selectorModel = MODEL_SINGLE

            //切换到加载中...
            setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_LOADING)

            //模拟网络操作
            dslViewHolder.postDelay(1000) {
                setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_NONE)

                onRefresh()
            }
        }
    }

    override fun onRefresh() {
        super.onRefresh()
        dslAdapter.resetItem(listOf())
        renderAdapter {

            for (i in 0..nextInt(20, 60)) {
                dslItem(DslDemoItem()) {

                    //初始化固定列表
                    if (i < 10 && i % 3 == 0) {
                        fixedItemList.add(this)
                    }

                    onSetItemOffset = {
                        val offset = 10 * dpi
                        it.set(0, offset, offset, 0)
                        itemGroupParams.apply {
                            if (isEdgeLeft()) {
                                it.left = 10 * dpi
                            }
                            if (isEdgeGroupBottom()) {
                                it.bottom = 10 * dpi
                            }
                        }
                    }
                    onItemBindOverride = { itemHolder, itemPosition, adapterItem ->
                        itemHolder.itemView.apply {
                            setBackgroundColor(
                                when {
                                    fixedItemList.contains(adapterItem) -> Color.GRAY
                                    itemIsSelectorInner -> Color.GREEN
                                    else -> Color.WHITE
                                }
                            )
                        }
                        itemHolder.tv(R.id.text_view).apply {
                            height = 100 * dpi
                            text =
                                "选我 $itemPosition \n${if (itemIsSelectorInner) "true" else "false"}"
                        }
                    }
                    onItemClick = {
                        updateItemSelector(!itemIsSelectorInner)
                    }
                }
            }

            dslViewHolder.postDelay(60) {
                //adapter的数据源是异步diff才显示到界面上的,所以加一个延迟

                //固定选项
                itemSelectorHelper.fixedSelectorItemList = fixedItemList
            }
        }
    }
}
