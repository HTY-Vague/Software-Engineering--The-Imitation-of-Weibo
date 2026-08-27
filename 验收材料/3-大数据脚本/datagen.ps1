# 生成业务表 CSV 数据（演示级，几千条）
# 输出：spark-stats/data/*.csv
# 前置步骤：先用 PassGen 生成 user_hashes.txt 放入 spark-stats/data/
#          javac PassGen.java && java PassGen （项目内置 BCrypt strength=10 加密）
# 用法：在仓库根执行：powershell -File spark-stats/datagen.ps1

$ErrorActionPreference = 'Stop'
$root = (Get-Item -Path $PSScriptRoot).Parent.FullName
$outDir = Join-Path $PSScriptRoot 'data'

# 在删除前先读取用户密码哈希文件（由 PassGen.java 提前生成）
$hashFile = Join-Path $outDir 'user_hashes.txt'
if (-not (Test-Path $hashFile)) {
    Write-Error "缺少 user_hashes.txt，请先运行 PassGen 生成密码哈希（参考 spark-stats/datagen.ps1 文件头注释）"
    exit 1
}
$userHashes = @{}
foreach ($line in (Get-Content -Path $hashFile -Encoding UTF8)) {
    $parts = $line.Split(',', 2)
    if ($parts.Count -ge 2) { $userHashes[$parts[0]] = $parts[1] }
}
Write-Host "已加载 $($userHashes.Count) 个用户密码哈希"

# 清空并重建输出目录
if (Test-Path $outDir) { Remove-Item -Recurse -Force $outDir }
New-Item -ItemType Directory -Path $outDir | Out-Null

$utf8NoBom = New-Object System.Text.UTF8Encoding $false

$now = Get-Date
function RandTime($daysAgoMin, $daysAgoMax) {
    $days = Get-Random -Minimum $daysAgoMin -Maximum ($daysAgoMax + 1)
    $secs = Get-Random -Minimum 0 -Maximum 86400
    return $now.AddDays(-$days).AddSeconds(-$secs).ToString('yyyy-MM-dd HH:mm:ss')
}
function FmtTime($dt) { return $dt.ToString('yyyy-MM-dd HH:mm:ss') }

# ============ 1. sys_user 50 行 ============
$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine('user_id,username,password,nickname,avatar,signature,role,status,create_time,update_time')
for ($i = 1; $i -le 50; $i++) {
    if ($i -eq 1) {
        $uname = 'admin'; $nick = '管理员'; $role = 'ADMIN'
        $ct = $now.AddDays(-30).ToString('yyyy-MM-dd HH:mm:ss')
    } elseif ($i -eq 2) {
        $uname = 'demo_user'; $nick = '演示用户'; $role = 'USER'
        $ct = $now.AddDays(-29).ToString('yyyy-MM-dd HH:mm:ss')
    } else {
        $uname = "user$($i-2)"; $nick = "用户$($i-2)"; $role = 'USER'
        $ct = RandTime 5 25
    }
    $pw = $userHashes[$uname]
    if (-not $pw) { Write-Error "缺少 $uname 的密码哈希" ; exit 2 }
    $line = "$i,$uname,$pw,$nick,,$role,1,$ct,$ct"
    [void]$sb.AppendLine($line)
}
[System.IO.File]::WriteAllText((Join-Path $outDir 'sys_user.csv'), $sb.ToString(), $utf8NoBom)
Write-Host "sys_user.csv  -> 50 rows"

# ============ 2. forum_board 5 行 ============
$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine('board_id,board_name,description,sort_num,status,create_time,update_time')
$boards = @(
    @(1,'公告板','课程设计公告与系统说明',1),
    @(2,'技术交流','后端、前端、数据库与部署问题交流',2),
    @(3,'校园生活','校园动态与日常分享',3),
    @(4,'学习笔记','课程与自学笔记整理',4),
    @(5,'闲聊灌水','水区，轻松交流',5)
)
foreach ($b in $boards) {
    $ct = $now.AddDays(-30).ToString('yyyy-MM-dd HH:mm:ss')
    [void]$sb.AppendLine("$($b[0]),$($b[1]),$($b[2]),$($b[3]),1,$ct,$ct")
}
[System.IO.File]::WriteAllText((Join-Path $outDir 'forum_board.csv'), $sb.ToString(), $utf8NoBom)
Write-Host "forum_board.csv -> 5 rows"

# ============ 3. forum_post 200 行 ============
$subjects = @('Spring Boot','MyBatis','MySQL','JVM','Vue3','Redis','Spark','Hadoop','Docker','Git','多线程','索引','事务','缓存','前后端分离','接口设计','异常处理','权限校验','JWT','部署')
$types    = @('学习心得','踩坑记录','实战总结','小技巧分享','求助提问','讨论交流','源码笔记','面试笔记','最佳实践','常见误区')
$bodyPhrases = @('最近在学这个,整理了一些笔记,欢迎大家交流指正。','工作中遇到这个问题,记录一下排查过程。','课程设计需要实现相关功能,贴出来分享一下思路。','遇到一个奇怪的报错,有大佬帮忙看看吗？','把官方文档的关键点提炼了一下,方便复习。','结合源码梳理了一遍流程,分享给大家。','面试被问到这个点,做个总结。','总结几条实践建议,供参考。','记录一个常见误区,新手容易踩。','分享一下我的做法,欢迎补充。')

$postTimes = @()
$postBoard = @()
$postAuthor = @()
$postTitle = @()

$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine('post_id,board_id,author_id,title,content,view_count,like_count,comment_count,is_top,status,create_time,update_time')
# post 1: 公告置顶帖
$postTimes += $now.AddDays(-30).ToString('yyyy-MM-dd HH:mm:ss')
$postBoard += 1
$postAuthor += 1
$postTitle += '欢迎使用仿微博论坛系统'
[void]$sb.AppendLine("1,1,1,欢迎使用仿微博论坛系统,这是课程设计演示环境的置顶欢迎帖,30,2,0,1,1,$($postTimes[0]),$($postTimes[0])")

for ($i = 2; $i -le 200; $i++) {
    $bid = Get-Random -Minimum 2 -Maximum 6   # board 2-5
    # 帖子作者偏普通用户
    $aid = if ($i -lt 10) { Get-Random -Minimum 1 -Maximum 51 } else { Get-Random -Minimum 3 -Maximum 51 }
    $subj = $subjects[(Get-Random -Maximum $subjects.Count)]
    $tp = $types[(Get-Random -Maximum $types.Count)]
    $title = "[$subj]$tp"
    $body = $bodyPhrases[(Get-Random -Maximum $bodyPhrases.Count)]
    $ct = RandTime 1 28
    $view = Get-Random -Minimum 5 -Maximum 201
    $like = Get-Random -Minimum 0 -Maximum 31
    $cmt = Get-Random -Minimum 0 -Maximum 16
    $isTop = if ($i -in @(50,100)) { 1 } else { 0 }
    $postTimes += $ct
    $postBoard += $bid
    $postAuthor += $aid
    $postTitle += $title
    [void]$sb.AppendLine("$i,$bid,$aid,$title,`"$body`",$view,$like,$cmt,$isTop,1,$ct,$ct")
}
[System.IO.File]::WriteAllText((Join-Path $outDir 'forum_post.csv'), $sb.ToString(), $utf8NoBom)
Write-Host "forum_post.csv -> 200 rows"

# ============ 4. forum_comment 800 行 ============
$cmtPool = @('说得太好了','学到了感谢楼主','我也遇到同样问题,后来怎么解决的？','mark 一下回头细看','请问博主有示例代码吗？','这个方案生产环境可行吗？','我也踩过这个坑','补充一点其实还可以用别的方式','总结得到位','跟我理解一致','有空也试试','收藏了慢慢看','期待下一篇','这个和官方文档不太一样啊','能贴下版本吗？我复现不了','课程设计帮大忙了','给楼主点赞','还要考虑并发场景吧','楼上说得对','边界情况当数据量大了会怎样')

# 一级评论:每帖至少1条 comment_id 1..200
$postFirstComment = @{}   # post_id -> list of comment_id
for ($p = 1; $p -le 200; $p++) { $postFirstComment[$p] = New-Object System.Collections.Generic.List[int] }

$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine('comment_id,post_id,user_id,parent_comment_id,reply_user_id,content,like_count,status,create_time,update_time')

$cid = 0
$commentUser = New-Object System.Collections.Generic.List[int]
$commentPost = New-Object System.Collections.Generic.List[int]

for ($p = 1; $p -le 200; $p++) {
    $cid++
    $uid = Get-Random -Minimum 1 -Maximum 51
    $content = $cmtPool[(Get-Random -Maximum $cmtPool.Count)]
    $baseTime = [datetime]::Parse($postTimes[$p-1]).AddHours((Get-Random -Minimum 1 -Maximum 120)).ToString('yyyy-MM-dd HH:mm:ss')
    $likec = Get-Random -Minimum 0 -Maximum 11
    [void]$sb.AppendLine("$cid,$p,$uid,,,`"$content`",$likec,1,$baseTime,$baseTime")
    $postFirstComment[$p].Add($cid)
    $commentUser.Add($uid)
    $commentPost.Add($p)
}

# 剩余 600 条:201..800
for ($k = 201; $k -le 800; $k++) {
    $cid++
    $p = Get-Random -Minimum 1 -Maximum 201
    $uid = Get-Random -Minimum 1 -Maximum 51
    $content = $cmtPool[(Get-Random -Maximum $cmtPool.Count)]
    $baseTime = [datetime]::Parse($postTimes[$p-1]).AddHours((Get-Random -Minimum 1 -Maximum 168)).ToString('yyyy-MM-dd HH:mm:ss')
    $likec = Get-Random -Minimum 0 -Maximum 11
    $parent = ''
    $reply = ''
    # 15% 概率二级回复
    if ((Get-Random -Maximum 100) -lt 15 -and $postFirstComment[$p].Count -gt 0) {
        $parentId = $postFirstComment[$p][(Get-Random -Maximum $postFirstComment[$p].Count)]
        $parent = $parentId
        $reply = $commentUser[$parentId-1]
    }
    [void]$sb.AppendLine("$cid,$p,$uid,$parent,$reply,`"$content`",$likec,1,$baseTime,$baseTime")
    $commentUser.Add($uid)
    $commentPost.Add($p)
    if ([string]::IsNullOrEmpty($parent)) { $postFirstComment[$p].Add($cid) }
}
[System.IO.File]::WriteAllText((Join-Path $outDir 'forum_comment.csv'), $sb.ToString(), $utf8NoBom)
Write-Host "forum_comment.csv -> 800 rows"

# ============ 5. forum_like 500 行 (300 帖子 + 200 评论) ============
$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine('like_id,target_type,target_id,user_id,create_time')
$seen = New-Object System.Collections.Generic.HashSet[string]
$lid = 0

function AddLike($type, $tidTarget, $users, $count) {
    foreach ($n in 1..$count) {
        for ($attempt = 0; $attempt -lt 20; $attempt++) {
            $tid = Get-Random -Minimum 1 -Maximum ($tidTarget + 1)
            $uid = Get-Random -Minimum 1 -Maximum 51
            $key = "$type|$tid|$uid"
            if ($seen.Add($key)) {
                $script:lid++
                $ct = RandTime 0 30
                [void]$script:sb.AppendLine("$script:lid,$type,$tid,$uid,$ct")
                break
            }
        }
    }
}
AddLike 1 200 $null 300
AddLike 2 800 $null 200
[System.IO.File]::WriteAllText((Join-Path $outDir 'forum_like.csv'), $sb.ToString(), $utf8NoBom)
Write-Host "forum_like.csv -> 500 rows"

Write-Host ""
Write-Host "Done. CSV at: $outDir"
Get-ChildItem $outDir | ForEach-Object { "{0,-22} {1,8:N0} bytes" -f $_.Name, $_.Length }