# centos 启动脚本，需要修改你的进程名
# 获取进程名
process_name=aj-base.jar

# 查找进程 ID
pid=$(jps -l | grep $process_name | awk '{print $1}')

# 打印进程 ID
echo "进程 ID 为：$pid"

# 判断进程 ID 是否为空
if [ -n "$pid" ]; then
    # 终止进程
    kill -9 $pid
    echo "停止进程 $pid"
else
    echo "没有找到进程 $process_name"
fi

echo "启动程序"
nohup java -Xms512m -Xmx512m -jar ./$process_name >message.log 2>&1 &
tail -f message.log