#!/bin/bash

# 获取当前分支名
current_branch=$(git rev-parse --abbrev-ref HEAD)

# 检查是否在 Git 仓库中
if [ $? -ne 0 ]; then
    echo "错误: 不在 Git 仓库目录中。"
    exit 1
fi

echo "当前分支: $current_branch"

# 遍历所有 remote 并执行 pull
for remote in $(git remote); do
    echo
    echo "从远程仓库拉取: $remote 分支: $current_branch"
    git pull "$remote" "$current_branch"
done

echo
echo "所有远程仓库已拉取。"
read -p "按任意键继续..." -n1 -s