import { useState, useEffect } from 'react';
import { Table, Card, Button, Space, message, Tag, Modal } from 'antd';
import { ReloadOutlined, CheckOutlined, DownloadOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { getFinancialList, confirmSettlement, exportFinancial } from '../../api/financial';
import type { Financial, FinancialQuery } from '../../types';
import { formatDate, getSettlementStatusText, formatCurrency } from '../../utils';

const SettlementList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<Financial[]>([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async (page = 1, pageSize = 10) => {
    try {
      setLoading(true);
      const response = await getFinancialList({ page, pageSize } as FinancialQuery);
      setDataSource(response.data.list);
      setPagination({
        current: response.data.page,
        pageSize: response.data.pageSize,
        total: response.data.total,
      });
    } catch {
      message.error('获取结算列表失败');
    } finally {
      setLoading(false);
    }
  };

  const handleTableChange = (pag: TablePaginationConfig) => {
    if (pag.current && pag.pageSize) {
      fetchData(pag.current, pag.pageSize);
    }
  };

  const handleConfirm = (id: number) => {
    Modal.confirm({
      title: '确认结算',
      content: '确定要确认该结算单吗？',
      onOk: async () => {
        try {
          await confirmSettlement(id);
          message.success('结算已确认');
          fetchData(pagination.current, pagination.pageSize);
        } catch {
          message.error('操作失败');
        }
      },
    });
  };

  const handleExport = async () => {
    try {
      const response = await exportFinancial();
      const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'settlement_list.xls';
      link.click();
      window.URL.revokeObjectURL(url);
      message.success('导出成功');
    } catch {
      message.error('导出失败');
    }
  };

  const getStatusColor = (status: number): string => {
    const colorMap: Record<number, string> = {
      0: 'default',
      1: 'processing',
      2: 'success',
      3: 'error',
    };
    return colorMap[status] || 'default';
  };

  const columns: ColumnsType<Financial> = [
    { title: '结算单号', dataIndex: 'settlementNo', key: 'settlementNo' },
    { title: '服务商', dataIndex: 'providerName', key: 'providerName' },
    { title: '结算周期', dataIndex: 'settlementPeriod', key: 'settlementPeriod', render: () => '-' },
    { title: '订单数量', dataIndex: 'orderCount', key: 'orderCount', render: () => '-' },
    {
      title: '结算金额',
      dataIndex: 'settlementAmount',
      key: 'settlementAmount',
      render: (amount: number) => formatCurrency(amount),
    },
    {
      title: '状态',
      dataIndex: 'settlementStatus',
      key: 'settlementStatus',
      render: (status: number) => (
        <Tag color={getStatusColor(status)}>{getSettlementStatusText(status)}</Tag>
      ),
    },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime', render: (text) => formatDate(text) },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="small">
          {record.settlementStatus === 0 && (
            <Button type="link" size="small" icon={<CheckOutlined />} onClick={() => handleConfirm(record.id)}>
              确认结算
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <Card
      title="财务结算管理"
      extra={
        <Space>
          <Button icon={<ReloadOutlined />} onClick={() => fetchData(pagination.current, pagination.pageSize)}>
            刷新
          </Button>
          <Button icon={<DownloadOutlined />} onClick={handleExport}>
            导出
          </Button>
        </Space>
      }
    >
      <Table
        columns={columns}
        dataSource={dataSource}
        rowKey="id"
        loading={loading}
        pagination={{
          ...pagination,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条`,
        }}
        onChange={handleTableChange}
      />
    </Card>
  );
};

export default SettlementList;
