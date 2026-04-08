import { useState, useEffect, useRef } from 'react';
import { Row, Col, Card, Statistic, Spin } from 'antd';
import {
  ShopOutlined,
  TeamOutlined,
  HeartOutlined,
  ShoppingCartOutlined,
  DollarOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  RiseOutlined,
} from '@ant-design/icons';
import { getDashboardStats } from '../api/statistics';
import type { DashboardStats } from '../types';
import { formatCurrency } from '../utils';
import * as echarts from 'echarts';

const Dashboard: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const chartRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetchStats();
  }, []);

  useEffect(() => {
    if (stats && chartRef.current) {
      const chart = echarts.init(chartRef.current);
      const option = {
        title: { text: '服务订单趋势', left: 'center' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['订单数', '收入'], top: 30 },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        },
        yAxis: [
          { type: 'value', name: '订单数' },
          { type: 'value', name: '收入(元)' },
        ],
        series: [
          {
            name: '订单数',
            type: 'line',
            smooth: true,
            data: [120, 132, 101, 134, 90, 230, 210],
            itemStyle: { color: '#1890ff' },
          },
          {
            name: '收入',
            type: 'line',
            smooth: true,
            yAxisIndex: 1,
            data: [2200, 1820, 1910, 2340, 2900, 3300, 3100],
            itemStyle: { color: '#52c41a' },
          },
        ],
      };
      chart.setOption(option);

      const handleResize = () => chart.resize();
      window.addEventListener('resize', handleResize);
      return () => {
        window.removeEventListener('resize', handleResize);
        chart.dispose();
      };
    }
  }, [stats]);

  const fetchStats = async () => {
    try {
      setLoading(true);
      const response = await getDashboardStats();
      setStats(response.data);
    } catch (error) {
      console.error('Failed to fetch dashboard stats:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '400px' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!stats) {
    return <div>暂无数据</div>;
  }

  return (
    <div>
      <h2 style={{ marginBottom: 24 }}>仪表盘</h2>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false}>
            <Statistic
              title="服务商总数"
              value={stats.totalProviders}
              prefix={<ShopOutlined />}
              suffix={<span style={{ fontSize: 14, color: '#52c41a' }}><RiseOutlined /> {stats.activeProviders}</span>}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false}>
            <Statistic
              title="服务人员总数"
              value={stats.totalStaff}
              prefix={<TeamOutlined />}
              suffix={<span style={{ fontSize: 14, color: '#52c41a' }}><RiseOutlined /> {stats.activeStaff}</span>}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false}>
            <Statistic
              title="老人客户总数"
              value={stats.totalElders}
              prefix={<HeartOutlined />}
              suffix={<span style={{ fontSize: 14, color: '#52c41a' }}><RiseOutlined /> {stats.activeElders}</span>}
              valueStyle={{ color: '#eb2f96' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false}>
            <Statistic
              title="订单总数"
              value={stats.totalOrders}
              prefix={<ShoppingCartOutlined />}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false}>
            <Statistic
              title="今日订单"
              value={stats.todayOrders}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false}>
            <Statistic
              title="今日收入"
              value={stats.todayRevenue}
              prefix={<DollarOutlined />}
              formatter={(value) => formatCurrency(Number(value))}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false}>
            <Statistic
              title="本月订单"
              value={stats.monthOrders}
              prefix={<ShoppingCartOutlined />}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card bordered={false}>
            <Statistic
              title="本月收入"
              value={stats.monthRevenue}
              prefix={<DollarOutlined />}
              formatter={(value) => formatCurrency(Number(value))}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card bordered={false} title="订单统计">
            <Row gutter={[16, 16]}>
              <Col span={12}>
                <Statistic
                  title="待处理订单"
                  value={stats.pendingOrders}
                  prefix={<ClockCircleOutlined />}
                  valueStyle={{ color: '#fa8c16' }}
                />
              </Col>
              <Col span={12}>
                <Statistic
                  title="已完成订单"
                  value={stats.completedOrders}
                  prefix={<CheckCircleOutlined />}
                  valueStyle={{ color: '#52c41a' }}
                />
              </Col>
            </Row>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card bordered={false} title="财务统计">
            <Row gutter={[16, 16]}>
              <Col span={8}>
                <Statistic
                  title="总收入"
                  value={stats.totalRevenue}
                  formatter={(value) => formatCurrency(Number(value))}
                  valueStyle={{ color: '#1890ff', fontSize: 20 }}
                />
              </Col>
              <Col span={8}>
                <Statistic
                  title="政府补贴"
                  value={stats.totalSubsidy}
                  formatter={(value) => formatCurrency(Number(value))}
                  valueStyle={{ color: '#722ed1', fontSize: 20 }}
                />
              </Col>
              <Col span={8}>
                <Statistic
                  title="自付金额"
                  value={stats.totalSelfPay}
                  formatter={(value) => formatCurrency(Number(value))}
                  valueStyle={{ color: '#eb2f96', fontSize: 20 }}
                />
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card bordered={false}>
            <div ref={chartRef} style={{ width: '100%', height: '350px' }}></div>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;
