/**
 * 接口自检脚本（只读，不改数据库）
 * 用途：Apifox 调不通时，先用它判断「是客户端配置问题，还是服务端问题」
 *
 * 运行：node exam-backend01/scripts/verify-api.mjs
 * 前提：后端已在 http://localhost:8080 启动
 */

const BASE = process.env.API_BASE || 'http://localhost:8080'

let pass = 0
let fail = 0

function check(name, ok, detail) {
  if (ok) {
    pass++
    console.log(`  [PASS] ${name}${detail ? ' -> ' + detail : ''}`)
  } else {
    fail++
    console.log(`  [FAIL] ${name}${detail ? ' -> ' + detail : ''}`)
  }
}

async function main() {
  console.log(`目标后端: ${BASE}\n`)

  // 0. 探活
  console.log('[0] 探活')
  try {
    const r = await fetch(`${BASE}/hello`)
    check('/hello 可访问', r.status === 200, `HTTP ${r.status}`)
  } catch (e) {
    console.log(`  [FAIL] 后端连不上：${e.message}`)
    console.log('  => 后端没启动，或端口不是 8080')
    process.exit(1)
  }

  // 1. 登录
  console.log('\n[1] 登录 POST /auth/login')
  const lr = await fetch(`${BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: '123456' }),
  })
  const lj = await lr.json()
  check('HTTP 200', lr.status === 200, `HTTP ${lr.status}`)
  check('biz code == 200', lj.code === 200, `code=${lj.code}`)
  const token = lj?.data?.token
  check('返回 data.token', !!token, token ? `长度 ${token.length}` : '缺失')
  if (!token) {
    console.log('\n登录失败，后续接口无法测试。请确认 admin 密码为 123456')
    process.exit(1)
  }
  console.log(`  token 前 30 位: ${token.slice(0, 30)}...`)
  const auth = { Authorization: `Bearer ${token}` }

  // 2. 鉴权对照实验（这是 Apifox 失败最可能的原因）
  console.log('\n[2] 鉴权对照')
  const noAuth = await fetch(`${BASE}/user/page?pageNum=1&pageSize=10`)
  const noAuthBody = await noAuth.text()
  check(
    '不带 token 应被拒绝',
    noAuth.status === 401 || noAuth.status === 403,
    `HTTP ${noAuth.status}，响应体长度 ${noAuthBody.length}`
  )
  if (noAuth.status === 403 && noAuthBody.length === 0) {
    console.log('  => 这就是 Apifox 里「接口失败、返回空白」的原因：缺 Authorization 头')
  }

  const badAuth = await fetch(`${BASE}/user/page?pageNum=1&pageSize=10`, {
    headers: { Authorization: 'Bearer abc.def.ghi' },
  })
  check('错误 token 应被拒绝', badAuth.status === 401 || badAuth.status === 403, `HTTP ${badAuth.status}`)

  // 3. U1 分页
  console.log('\n[3] U1 分页查询 GET /user/page')
  const p1 = await (await fetch(`${BASE}/user/page?pageNum=1&pageSize=10`, { headers: auth })).json()
  check('code == 200', p1.code === 200, `code=${p1.code}`)
  console.log(`  total=${p1.data.total} pages=${p1.data.pages} records=${p1.data.records.length}`)

  const p2 = await (await fetch(`${BASE}/user/page?pageNum=1&pageSize=2`, { headers: auth })).json()
  check('pageSize=2 生效', p2.data.records.length === 2, `records=${p2.data.records.length}`)
  check('total 不随 pageSize 变', p2.data.total === p1.data.total, `total=${p2.data.total}`)
  check('pages 计算正确', p2.data.pages === Math.ceil(p2.data.total / 2), `pages=${p2.data.pages}`)

  // 4. U2 条件搜索
  console.log('\n[4] U2 条件搜索 GET /user/page?username=teach')
  const s = await (await fetch(`${BASE}/user/page?pageNum=1&pageSize=10&username=teach`, { headers: auth })).json()
  const names = s.data.records.map((u) => u.username)
  check('按用户名模糊匹配', names.length > 0 && names.every((n) => n.includes('teach')), JSON.stringify(names))

  // 5. 角色列表
  console.log('\n[5] 角色列表 GET /user/roles')
  const ro = await (await fetch(`${BASE}/user/roles`, { headers: auth })).json()
  check('返回 3 个角色', ro.data.length === 3, JSON.stringify(ro.data.map((x) => x.roleCode)))

  // 5.5 基线清单（做增删改用例前先抄一份）
  console.log('\n[5.5] 基线用户清单（跑增删改用例前先抄下来，方便事后核对）')
  const all = await (await fetch(`${BASE}/user/page?pageNum=1&pageSize=100`, { headers: auth })).json()
  for (const u of all.data.records) {
    console.log(
      `  id=${u.id}  username=${u.username}  realName=${u.realName}  status=${u.status}  role=${u.roleCode || '(无关联)'}`
    )
  }
  console.log(`  合计 ${all.data.total} 条`)
  const maxId = all.data.records.length ? Math.max(...all.data.records.map((u) => u.id)) : 0
  console.log(`  当前最大 id = ${maxId}`)
  console.log(`  注意：新增用户后返回的 data 是 null，拿不到 id；请去列表第一行看实际主键，不要假设就是 ${maxId + 1}`)

  // 6. 数据体检
  console.log('\n[6] 数据体检（常见视觉异常）')
  const nulls = p1.data.records.filter((u) => !u.roleCode).map((u) => u.username)
  if (nulls.length) {
    console.log(`  注意：以下用户没有角色关联，前端「角色」列会显示空白：${nulls.join(', ')}`)
    console.log('  原因：sys_user_role 表里没有这些 user_id 的记录，不是代码 bug')
  } else {
    console.log('  所有用户都有角色关联')
  }

  console.log(`\n===== 结果：${pass} 通过 / ${fail} 失败 =====`)
  process.exit(fail ? 1 : 0)
}

main().catch((e) => {
  console.error('脚本异常：', e)
  process.exit(1)
})
