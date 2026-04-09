import { useState, useEffect, useRef, useCallback } from 'react';
import { Row, Col, Card, Statistic, Spin, Button } from 'antd';
import { ReloadOutlined, ShoppingCartOutlined, CheckCircleOutlined, ClockCircleOutlined } from '@ant-design/icons';
import { getOrderTrend, getServiceTypeStats, getStaffStats } from '../../api/statistics';
import type { ChartData } from '../../types';
import * as echarts from 'echarts';

const OrderStatistics: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [orderTrendData, setOrderTrendData] = useState<ChartData[]>([]);
  const [serviceTypeData, setServiceTypeData] = useState<ChartData[]>([]);
  const [staffData, setStaffData] = useState<ChartData[]>([]);
  const [totalOrders, setTotalOrders] = useState(0);
  const [completedOrders, setCompletedOrders] = useState(0);
  const [pendingOrders, setPendingOrders] = useState(0);
  const orderTrendRef = useRef<HTMLDivElement>(null);
  const serviceTypeRef = useRef<HTMLDivElement>(null);
  const staffStatsRef = useRef<HTMLDivElement>(null);
  const resizeHandlerRef = useRef<() => void>(() => {});

  useEffect(() => {
    fetchData();
  }, []);

  const handleResize = useCallback(() => {
    echarts.getInstanceByDom(orderTrendRef.current!)?.resize();
    echarts.getInstanceByDom(serviceTypeRef.current!)?.resize();
    echarts.getInstanceByDom(staffStatsRef.current!)?.resize();
  }, []);

  useEffect(() => {
    if (!loading) {
      initCharts();
    }
    return () => {
      window.removeEventListener('resize', resizeHandlerRef.current);
    };
  }, [loading, orderTrendData, serviceTypeData, staffData]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [trendRes, serviceTypeRes, staffRes] = await Promise.all([
        getOrderTrend(),
        getServiceTypeStats(),
        getStaffStats(),
      ]);
      setOrderTrendData(trendRes.data);
      setServiceTypeData(serviceTypeRes.data);
      setStaffData(staffRes.data);

      // Calculate totals
      const total = trendRes.data.reduce((sum, item) => sum + item.value, 0);
      setTotalOrders(total);
      setCompletedOrders(Math.floor(total * 0.8));
      setPendingOrders(Math.floor(total * 0.2));
    } catch (error) {
      console.error('Failed to fetch statistics:', error);
    } finally {
      setLoading(false);
    }
  };

  const initCharts = () => {
    // Order Trend Chart
    if (orderTrendRef.current) {
      const orderTrendChart = echarts.init(orderTrendRef.current);
      orderTrendChart.setOption({
        title: { text: '订单趋势', left: 'center' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['订单数'], top: 30 },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          data: orderTrendData.map(item => item.date),
        },
        yAxis: { type: 'value', name: '订单数' },
        series: [{
          name: '订单数',
          type: 'line',
          smooth: true,
          data: orderTrendData.map(item => item.value),
          itemStyle: { color: '#1890ff' },
        }],
      });
      orderTrendChart.resize();
    }

    // Service Type Chart
    if (serviceTypeRef.current) {
      const serviceTypeChart = echarts.init(serviceTypeRef.current);
      serviceTypeChart.setOption({
        title: { text: '服务类型分布', left: 'center' },
        tooltip: { trigger: 'item' },
        legend: { data: serviceTypeData.map(item => item.label), top: 30 },
        series: [{
          type: 'pie',
          radius: '50%',
          data: serviceTypeData.map(item => ({ name: item.label, value: item.value })),
        }],
      });
      serviceTypeChart.resize();
    }

    resizeHandlerRef.current = handleResize;
    window.addEventListener('resize', handleResize);
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '400px' }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div>
      <h2 style={{ marginBottom: 24 }}>订单统计</h2>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={8}>
          <Card bordered={false}>
            <Statistic
              title="总订单数"
              value={totalOrders}
              prefix={<ShoppingCartOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card bordered={false}>
            <Statistic
              title="已完成订单"
              value={completedOrders}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card bordered={false}>
            <Statistic
              title="待处理订单"
              value={pendingOrders}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card bordered={false} extra={<Button icon={<ReloadOutlined />} onClick={fetchData}>刷新</Button>}>
            <div ref={orderTrendRef} style={{ width: '100%', height: '350px' }}></div>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card bordered={false}>
            <div ref={serviceTypeRef} style={{ width: '100%', height: '350px' }}></div>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card bordered={false}>
            <div ref={staffStatsRef} style={{ width: '100%', height: '350px' }}></div>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default OrderStatistics;
