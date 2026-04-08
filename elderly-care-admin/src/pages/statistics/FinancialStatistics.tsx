import { useState, useEffect, useRef } from 'react';
import { Row, Col, Card, Statistic, Spin, Button } from 'antd';
import { ReloadOutlined, DollarOutlined, BankOutlined, WalletOutlined } from '@ant-design/icons';
import { getRevenueTrend, getProviderStats } from '../../api/statistics';
import type { ChartData } from '../../types';
import * as echarts from 'echarts';
import { formatCurrency } from '../../utils';

const FinancialStatistics: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [revenueTrendData, setRevenueTrendData] = useState<ChartData[]>([]);
  const [providerData, setProviderData] = useState<ChartData[]>([]);
  const [totalRevenue, setTotalRevenue] = useState(0);
  const [totalSubsidy, setTotalSubsidy] = useState(0);
  const [totalSelfPay, setTotalSelfPay] = useState(0);
  const revenueTrendRef = useRef<HTMLDivElement>(null);
  const providerStatsRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetchData();
  }, []);

  useEffect(() => {
    if (!loading) {
      initCharts();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, [loading, revenueTrendData, providerData]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [revenueRes, providerRes] = await Promise.all([
        getRevenueTrend(),
        getProviderStats(),
      ]);
      setRevenueTrendData(revenueRes.data);
      setProviderData(providerRes.data);

      // Calculate totals
      const revenue = revenueRes.data.reduce((sum, item) => sum + item.value, 0);
      setTotalRevenue(revenue);
      setTotalSubsidy(Math.floor(revenue * 0.6));
      setTotalSelfPay(Math.floor(revenue * 0.4));
    } catch (error) {
      console.error('Failed to fetch financial statistics:', error);
    } finally {
      setLoading(false);
    }
  };

  const initCharts = () => {
    // Revenue Trend Chart
    if (revenueTrendRef.current) {
      const revenueTrendChart = echarts.init(revenueTrendRef.current);
      revenueTrendChart.setOption({
        title: { text: '收入趋势', left: 'center' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['收入'], top: 30 },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          data: revenueTrendData.map(item => item.date),
        },
        yAxis: { type: 'value', name: '金额(元)' },
        series: [{
          name: '收入',
          type: 'line',
          smooth: true,
          data: revenueTrendData.map(item => item.value),
          itemStyle: { color: '#52c41a' },
          areaStyle: { color: 'rgba(82, 196, 26, 0.1)' },
        }],
      });
      revenueTrendChart.resize();
    }

    // Provider Stats Chart
    if (providerStatsRef.current) {
      const providerStatsChart = echarts.init(providerStatsRef.current);
      providerStatsChart.setOption({
        title: { text: '服务商收入排名', left: 'center' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['收入'], top: 30 },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          data: providerData.slice(0, 10).map(item => item.label),
          axisLabel: { rotate: 45 },
        },
        yAxis: { type: 'value', name: '金额(元)' },
        series: [{
          name: '收入',
          type: 'bar',
          data: providerData.slice(0, 10).map(item => item.value),
          itemStyle: { color: '#722ed1' },
        }],
      });
      providerStatsChart.resize();
    }

    const handleResize = () => {
      echarts.getInstanceByDom(revenueTrendRef.current!)?.resize();
      echarts.getInstanceByDom(providerStatsRef.current!)?.resize();
    };
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
      <h2 style={{ marginBottom: 24 }}>财务统计</h2>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={8}>
          <Card bordered={false}>
            <Statistic
              title="总收入"
              value={totalRevenue}
              prefix={<DollarOutlined />}
              formatter={(value) => formatCurrency(Number(value))}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card bordered={false}>
            <Statistic
              title="政府补贴"
              value={totalSubsidy}
              prefix={<BankOutlined />}
              formatter={(value) => formatCurrency(Number(value))}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card bordered={false}>
            <Statistic
              title="自付金额"
              value={totalSelfPay}
              prefix={<WalletOutlined />}
              formatter={(value) => formatCurrency(Number(value))}
              valueStyle={{ color: '#eb2f96' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card bordered={false} extra={<Button icon={<ReloadOutlined />} onClick={fetchData}>刷新</Button>}>
            <div ref={revenueTrendRef} style={{ width: '100%', height: '350px' }}></div>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card bordered={false}>
            <div ref={providerStatsRef} style={{ width: '100%', height: '350px' }}></div>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default FinancialStatistics;
