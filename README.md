[ ![Download](https://api.bintray.com/packages/teadoglibrary/LuckyCardView/LuckyCardViewHelper/images/download.svg) ](https://bintray.com/teadoglibrary/LuckyCardView/LuckyCardViewHelper/_latestVersion)

# LuckyCardView
一行代码让View变成刮刮乐，少侵入，简单集成

<img src="https://github.com/huzipiaopiao/LuckyCardView/blob/master/img/demo.gif" width="30%" height="30%">

# 注意事项：
- 1、不要再调用view的`setOnTouchListener()`方法和`setForeground()`方法，否则会导致无法刮开；
- 2、当项目的minSdkVersion小于23时，view要是FrameLayout，其他类型的View会导致遮挡层无效，所以可以先在你xml布局中用FrameLayout包裹原view，再将FrameLayout传入helper中；
- 3、当项目的minSdkVersion大于等于23时，view可以是任何View；

# 使用方法：
## 1、依赖配置
- 在项目最外面的build.gradle文件中，allprojects节点下的repositories中添加：

        maven { url  "https://dl.bintray.com/teadoglibrary/LuckyCardView"  }

- 再在app的build.gradle文件中，dependencies节点下添加，其中的版本建议根据最新版本修改：

        compile 'com.teaanddogdog:luckycardviewhelper:1.0.0'


## 2、代码中使用

// 创建helper

```LuckyCardViewHelper luckyCardViewHelper = new LuckyCardViewHelper();```

// 绑定view、刮刮乐顶部涂层资源、自动显示百分比、完成回调

```luckyCardViewHelper.init(view, R.drawable.lucky_card_foreground, 60, new LuckyCardViewHelperListener());```

// 重新恢复刮刮乐的顶部涂层

```luckyCardViewHelper.reset();```

# 思路：
- 给view设置一个前景背景`view.setForeground()`;



