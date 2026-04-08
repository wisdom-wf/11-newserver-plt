import { useState } from 'react';
import { Layout, Menu, Avatar, Dropdown, theme } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  DashboardOutlined,
  UserOutlined,
  ShopOutlined,
  TeamOutlined,
  HeartOutlined,
  ShoppingCartOutlined,
  DollarOutlined,
  StarOutlined,
  SettingOutlined,
  BarChartOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  LogoutOutlined,
  LockOutlined,
  ToolOutlined,
} from '@ant-design/icons';
import type { MenuProps } from 'antd';

const { Header, Sider, Content } = Layout;

const AppLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { token } = theme.useToken();

  const userStr = localStorage.getItem('user');
  const userInfo = (userStr && userStr !== 'undefined') ? JSON.parse(userStr) : {};

  const menuItems: MenuProps['items'] = [
    {
      key: '/',
      icon: <DashboardOutlined />,
      label: '首页/仪表盘',
    },
    {
      key: '/system',
      icon: <SettingOutlined />,
      label: '系统管理',
      children: [
        { key: '/system/users', label: '用户管理' },
        { key: '/system/roles', label: '角色管理' },
        { key: '/system/permissions', label: '权限管理' },
      ],
    },
    {
      key: '/provider',
      icon: <ShopOutlined />,
      label: '服务商管理',
    },
    {
      key: '/staff',
      icon: <TeamOutlined />,
      label: '服务人员管理',
    },
    {
      key: '/elder',
      icon: <HeartOutlined />,
      label: '老人客户管理',
    },
    {
      key: '/order',
      icon: <ShoppingCartOutlined />,
      label: '订单管理',
    },
    {
      key: '/financial',
      icon: <DollarOutlined />,
      label: '财务结算',
    },
    {
      key: '/evaluation',
      icon: <StarOutlined />,
      label: '服务评价',
    },
    {
      key: '/config',
      icon: <ToolOutlined />,
      label: '系统配置',
      children: [
        { key: '/config/dict-types', label: '字典类型' },
        { key: '/config/areas', label: '区域管理' },
        { key: '/config/service-types', label: '服务类型' },
      ],
    },
    {
      key: '/statistics',
      icon: <BarChartOutlined />,
      label: '数据统计',
      children: [
        { key: '/statistics/order', label: '订单统计' },
        { key: '/statistics/financial', label: '财务统计' },
      ],
    },
  ];

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人中心',
    },
    {
      key: 'password',
      icon: <LockOutlined />,
      label: '修改密码',
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
    },
  ];

  const handleMenuClick: MenuProps['onClick'] = (e) => {
    if (e.key.startsWith('/')) {
      navigate(e.key);
    }
  };

  const handleUserMenuClick: MenuProps['onClick'] = ({ key }) => {
    if (key === 'logout') {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      navigate('/login');
    } else if (key === 'profile') {
      navigate('/profile');
    } else if (key === 'password') {
      navigate('/password');
    }
  };

  const getSelectedKeys = () => {
    const path = location.pathname;
    if (path === '/') return ['/'];
    if (path.split('/').length > 2) {
      return ['/' + path.split('/')[1] + '/' + path.split('/')[2]];
    }
    return ['/' + path.split('/')[1]];
  };

  const getOpenKeys = () => {
    const path = location.pathname;
    if (path.startsWith('/system')) return ['/system'];
    if (path.startsWith('/provider')) return ['/provider'];
    if (path.startsWith('/staff')) return ['/staff'];
    if (path.startsWith('/elder')) return ['/elder'];
    if (path.startsWith('/order')) return ['/order'];
    if (path.startsWith('/financial')) return ['/financial'];
    if (path.startsWith('/evaluation')) return ['/evaluation'];
    if (path.startsWith('/config')) return ['/config'];
    if (path.startsWith('/statistics')) return ['/statistics'];
    return [];
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed}>
        <div style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#fff',
          fontSize: collapsed ? 14 : 18,
          fontWeight: 'bold',
          background: 'rgba(255,255,255,0.1)',
          margin: 16,
          borderRadius: 8,
        }}>
          {collapsed ? '养老' : '智慧居家养老'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={getSelectedKeys()}
          defaultOpenKeys={getOpenKeys()}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header style={{
          padding: '0 16px',
          background: token.colorBgContainer,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
        }}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            {collapsed ? (
              <MenuUnfoldOutlined style={{ fontSize: 18, cursor: 'pointer' }} onClick={() => setCollapsed(!collapsed)} />
            ) : (
              <MenuFoldOutlined style={{ fontSize: 18, cursor: 'pointer' }} onClick={() => setCollapsed(!collapsed)} />
            )}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <span style={{ color: token.colorTextSecondary }}>
              {userInfo.realName || userInfo.username}
            </span>
            <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenuClick }} placement="bottomRight">
              <Avatar style={{ cursor: 'pointer' }} icon={<UserOutlined />} />
            </Dropdown>
          </div>
        </Header>
        <Content style={{ margin: 16, padding: 16, background: token.colorBgContainer, borderRadius: 8 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default AppLayout;
