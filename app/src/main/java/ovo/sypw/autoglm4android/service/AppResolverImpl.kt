package ovo.sypw.autoglm4android.service

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 应用解析器实现
 * 将应用名称解析为包名
 */
@Singleton
class AppResolverImpl @Inject constructor() : ovo.sypw.autoglm4android.domain.repository.AppResolver {

    // 常用应用映射表
    private val appMapping = mapOf(
        "微信" to "com.tencent.mm",
        "wechat" to "com.tencent.mm",
        "qq" to "com.tencent.mobileqq",
        "支付宝" to "com.eg.android.AlipayGphone",
        "alipay" to "com.eg.android.AlipayGphone",
        "淘宝" to "com.taobao.taobao",
        "taobao" to "com.taobao.taobao",
        "抖音" to "com.ss.android.ugc.aweme",
        "douyin" to "com.ss.android.ugc.aweme",
        "微博" to "com.sina.weibo",
        "weibo" to "com.sina.weibo",
        "京东" to "com.jingdong.app.mall",
        "jd" to "com.jingdong.app.mall",
        "拼多多" to "com.xunmeng.pinduoduo",
        "pinduoduo" to "com.xunmeng.pinduoduo",
        "小红书" to "com.xingin.xhs",
        "xiaohongshu" to "com.xingin.xhs",
        "bilibili" to "com.bilibili.app.in",
        "b站" to "com.bilibili.app.in",
        "网易云音乐" to "com.netease.cloudmusic",
        "spotify" to "com.spotify.music",
        "设置" to "com.android.settings",
        "settings" to "com.android.settings",
        "相机" to "com.android.camera",
        "camera" to "com.android.camera",
        "相册" to "com.android.gallery3d",
        "gallery" to "com.android.gallery3d",
        "电话" to "com.android.dialer",
        "dialer" to "com.android.dialer",
        "短信" to "com.android.mms",
        "messages" to "com.android.mms",
        "浏览器" to "com.android.browser",
        "browser" to "com.android.browser",
        "chrome" to "com.android.chrome",
        "gmail" to "com.google.android.gm",
        "地图" to "com.google.android.apps.maps",
        "maps" to "com.google.android.apps.maps",
        "youtube" to "com.google.android.youtube",
        "优酷" to "com.youku.phone",
        "youku" to "com.youku.phone",
        "爱奇艺" to "com.qiyi.video",
        "iqiyi" to "com.qiyi.video",
        "美团" to "com.sankuai.meituan",
        "meituan" to "com.sankuai.meituan",
        "饿了么" to "me.ele",
        "eleme" to "me.ele",
        "滴滴" to "com.sdu.didi.psnger",
        "didi" to "com.sdu.didi.psnger",
        "高德地图" to "com.autonavi.minimap",
        "amap" to "com.autonavi.minimap",
        "百度地图" to "com.baidu.BaiduMap",
        "baidumap" to "com.baidu.BaiduMap",
        "钉钉" to "com.alibaba.android.rimet",
        "dingtalk" to "com.alibaba.android.rimet",
        "飞书" to "com.ss.android.lark",
        "feishu" to "com.ss.android.lark",
        "企业微信" to "com.tencent.wework",
        "wework" to "com.tencent.wework",
        "知乎" to "com.zhihu.android",
        "zhihu" to "com.zhihu.android",
        "今日头条" to "com.ss.android.article.news",
        "toutiao" to "com.ss.android.article.news",
        "快手" to "com.smile.gifmaker",
        "kuaishou" to "com.smile.gifmaker",
        "淘宝特价版" to "com.taobao.litetao",
        "闲鱼" to "com.taobao.idlefish",
        "转转" to "com.wuba.zhuanzhuan",
        "58同城" to "com.wuba",
        "赶集网" to "com.ganji.android",
        "携程" to "ctrip.android.view",
        "ctrip" to "ctrip.android.view",
        "去哪儿" to "com.Qunar",
        "飞猪" to "com.taobao.trip",
        "12306" to "com.MobileTicket",
        "铁路12306" to "com.MobileTicket",
        "滴滴出行" to "com.sdu.didi.psnger",
        "哈啰出行" to "com.jingyao.easybike",
        "美团外卖" to "com.sankuai.meituan.takeoutnew",
        "饿了么外卖" to "me.ele",
        "大众点评" to "com.dianping.v1",
        "dianping" to "com.dianping.v1",
        "贝壳找房" to "com.lianjia.beike",
        "链家" to "com.lianjia.beike",
        "安居客" to "com.anjuke.android.app",
        "58同城租房" to "com.wuba",
        "boss直聘" to "com.hpbr.bosszhipin",
        "boss" to "com.hpbr.bosszhipin",
        "智联招聘" to "com.zhaopin.social",
        "前程无忧" to "com.job.android",
        "猎聘" to "com.lietou.mishu",
        "脉脉" to "com.taou.maimai",
        "领英" to "com.linkedin.android",
        "linkedin" to "com.linkedin.android",
        "抖音火山版" to "com.ss.android.ugc.live",
        "抖音极速版" to "com.ss.android.ugc.aweme.lite",
        "快手极速版" to "com.kuaishou.nebula",
        "西瓜视频" to "com.ss.android.article.video",
        "好看视频" to "com.baidu.haokan",
        "腾讯视频" to "com.tencent.qqlive",
        "iqiyi" to "com.qiyi.video",
        "芒果tv" to "com.hunantv.imgo.activity",
        "咪咕视频" to "com.cmcc.cmvideo",
        "咪咕音乐" to "com.cmcc.music",
        "qq音乐" to "com.tencent.qqmusic",
        "酷狗音乐" to "com.kugou.android",
        "酷我音乐" to "cn.kuwo.player",
        "喜马拉雅" to "com.ximalaya.ting.android",
        "懒人听书" to "bubei.tingshu",
        "得到" to "com.luojilab.player",
        "知乎" to "com.zhihu.android",
        "豆瓣" to "com.douban.frodo",
        "贴吧" to "com.baidu.tieba",
        "虎扑" to "com.hupu.games",
        "懂球帝" to "com.dongqiudi.news",
        "直播吧" to "com.zhibo8",
        "虎牙直播" to "com.duowan.kiwi",
        "斗鱼" to "air.tv.douyu.android",
        "企鹅电竞" to "com.tencent.gamehelper.pubgm",
        "王者荣耀" to "com.tencent.tmgp.sgame",
        "和平精英" to "com.tencent.tmgp.pubgmhd",
        "原神" to "com.miHoYo.Yuanshen",
        "崩坏星穹铁道" to "com.miHoYo.hkrpg",
        "明日方舟" to "com.hypergryph.arknights",
        "阴阳师" to "com.netease.onmyoji",
        "梦幻西游" to "com.netease.mhxy",
        "大话西游" to "com.netease.dhxy"
    )

    override suspend fun resolvePackageName(appName: String): String? {
        val normalizedName = appName.trim().lowercase()

        // 精确匹配
        appMapping[normalizedName]?.let { return it }

        // 部分匹配
        appMapping.entries.find { (key, _) ->
            key.contains(normalizedName) || normalizedName.contains(key)
        }?.let { return it.value }

        return null
    }

    override suspend fun resolveAppName(packageName: String): String? {
        return appMapping.entries.find { it.value == packageName }?.key
    }
}
