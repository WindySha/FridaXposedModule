## What is this
这是一个用于在非Root的Android设备上使用[Frida](https://github.com/frida/frida)的一个Xposed模块。

## How to use it
为了在免Root环境中使用这个Xposed模块，有两种方法来使用这个模块。
1. 使用笔者开发的[Xpatch](https://github.com/WindySha/Xpatch)工具，或者其App版本Xposed Tool[(点击下载)](https://xposed-tool-app.oss-cn-beijing.aliyuncs.com/data/xposed_tool_v2.0.2.apk)，
对需要使用Frida的App进行重打包，以植入加载Xposed模块的代码。然后卸载设备上的原App，安装重打包后的App，然后再安装此Xposed模块即可。
2. 在支持Xposed的双开环境下启动原App，比如[SandVXposed](https://github.com/ganyao114/SandVXposed)，然后在双开环境中安装此Xposed模块，并启用它即可；
此方法未测试，原则上是可行。
