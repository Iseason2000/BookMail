# Minecraft插件-BookMail——书里的邮件 

## 介绍

这是一款以书本为媒介的邮箱系统。

### 目前功能

1. **书内类markdown语法与简便格式化代码**：

   ![mcplugin_bookMail_md](https://img.imgdb.cn/item/6028e1d2d2a061fec7d90f35.jpg)

2. **群发功能**

   指定 **n个人** 、**所有在线玩家**、**所有离线玩家**、**所有注册玩家** 、**新注册的玩家**、**指定时间内登录过的玩家**发送系统邮件。

3. **定时发送功能**

   **一次性** **定时** 自动发送 与 **周期性** **定时**发送  定时发送兼容群发功能 

4. **书内管理界面**

   例如：玩家邮箱

   ![mcplugin_bookMail_playerMail](https://img.imgdb.cn/item/6028e1d2d2a061fec7d90f2c.jpg)

5. **物品包裹**（**支持nbt物品**）

      支持限制玩家物品堆数量（包括潜影盒里的也算）

6. **指定物品打开邮箱**

      可选设置一个物品，主手拿着物品邮件空气以打开自己的邮箱。

### 使用详解

要发送一封邮件，载体为**成书**，成书的**作者**为**发送玩家**，**标题**为**邮件标题**  拿着**书与笔或成书**可以预览内容

### 一、类markdown语法

~~~ minecraft
悬浮事件
显示文字：显示原始JSON文本对象。
{文字}(显示的文字)

点击事件
打开链接 :打开链接 悬浮显示网址
[文字](链接)
运行命令：运行命令
[文字][命令]
复制: 复制到剪贴板
[文字]{复制的内容}
包裹
(领取包裹){包裹id}
&样式代码
~~~



#### 二、 玩家

~~~ minecraft
/bookmail open 打开邮箱 或者主手拿邮箱物品右键空气
/bookmail package 查看包裹相关操作
/bookmail send 玩家ID 将手上的成书作为邮件发送给某个玩家
/bookmail translate 预览手上的成书或者书与笔转化为邮件的效果（包裹不可领）
~~~

#### 三、管理员

建议在使用前先把所有命令详解看一看

如果是途中添加插件的服务器，可以输入/bookmail system createMailboxes online 来注册服里在线的玩家（注册邮箱）

插件运行之后 会自动为新登录的且没有邮箱的玩家创建邮箱

~~~ minecraft
/bookmail 查看所有可用指令
/bookmail package 查看查看包裹相关操作
/bookmail sendGroup 查看群发相关操作
/bookmail sendGroup loginTime 查看群发登录时间详解
/bookmail sendGroup sendOnTime 查看定时发送功能详解
/bookmail system 查看系统功能详解
~~~

P.S. 一天时间从**00:00:00 开始 23:59:59 结束**

部分难懂的例子：

**想要指定时间段内登录过的玩家发送邮件：**

/bookmail sendGroup loginTime 参数1 参数2

参数可以是**xs xm xh xd** 表示x 秒 x 分钟 x小时 x 天 可以**单个**也可以**任意组合**(不能有空格) 最后的时间点将会是当前时间 减去所有参数表示的时间。

具体时间点 **yyyy-MM-dd-HH:mm:ss** 例如: 2021-08-10-10:23:08 不会有人看不懂吧？

参数2 没有的话就是当前时间 

最后将会抽取在2个时间点之间登录的玩家发送邮件（顺序任意）

**想要每天18:00:00 发送邮件**

/bookmail sendOnTime period day:1+18:00:00 online

/bookmail sendOnTime 参数1 参数2+时间 发送类型

| 类型  | 参数1  | 参数2   | 时间                            | 发送类型   |
| ----- | ------ | ------- | ------------------------------- | ---------- |
| 一次  | once   | 无      | yyyy-MM-dd HH:mm:ss 或 1d /5m1d | 与群发相同 |
| 每x天 | period | day:x   | HH:mm:ss                        | 与群发相同 |
| 每x月 | period | month:x | dd-HH:mm:ss                     | 与群发相同 |

​    发送类型不包括 新注册玩家(new) 登录时间的参数为loginTime[参数1,参数2]
