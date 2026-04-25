#!/bin/bash

# 设置 UTF-8 编码 (macOS 终端通常默认支持)
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8

# 一键 Push 到所有 Remote
# 获取当前分支名
current_branch=$(git rev-parse --abbrev-ref HEAD 2>/dev/null)

# 检查是否在 Git 仓库中
if [ $? -ne 0 ] || [ -z "$current_branch" ]; then
    echo "❌ 错误：当前目录不是一个 Git 仓库！"
    read -p "按任意键退出..." -n1 -s
    exit 1
fi

echo "当前分支: $current_branch"

# 遍历所有 remote 并执行 push
for remote in $(git remote); do
    echo
    echo "正在推送到远程仓库: $remote 分支: $current_branch"
    git push "$remote" "$current_branch"
done

echo
echo "✅ 已推送到所有远程仓库。"
read -p "按任意键退出..." -n1 -s