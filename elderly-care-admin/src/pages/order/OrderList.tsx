import { useState, useEffect } from 'react';
import { Table, Card, Button, Space, message, Tag, Modal, Form, Input, DatePicker } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, PlayCircleOutlined, CheckCircleOutlined, StopOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { getOrderList, createOrder, updateOrder, deleteOrder, cancelOrder, startService, completeService } from '../../api/order';
import type { Order, OrderQuery } from '../../types';
import { getOrderStatusText, formatCurrency, formatDate } from '../../utils';

const OrderList: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<Order[]>([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingOrder, setEditingOrder] = useState<Order | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async (page = 1, pageSize = 10) => {
    try {
      setLoading(true);
      const response = await getOrderList({ page, pageSize } as OrderQuery);
      setDataSource(response.data.list);
      setPagination({
        current: response.data.page,
        pageSize: response.data.pageSize,
        total: response.data.total,
      });
    } catch {
      message.error('获取订单列表失败');
    } finally {
      setLoading(false);
    }
  };

  const handleTableChange = (pag: TablePaginationConfig) => {
    if (pag.current && pag.pageSize) {
      fetchData(pag.current, pag.pageSize);
    }
  };

  const handleAdd = () => {
    setEditingOrder(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: Order) => {
    setEditingOrder(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = (id: string) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该订单吗？',
      onOk: async () => {
        try {
          await deleteOrder(id);
          message.success('删除成功');
          fetchData(pagination.current, pagination.pageSize);
        } catch {
          message.error('删除失败');
        }
      },
    });
  };

  const handleCancel = (id: string) => {
    Modal.confirm({
      title: '确认取消',
      content: '确定要取消该订单吗？',
      onOk: async () => {
        try {
          await cancelOrder(id);
          message.success('订单已取消');
          fetchData(pagination.current, pagination.pageSize);
        } catch {
          message.error('操作失败');
        }
      },
    });
  };

  const handleStart = async (id: string) => {
    try {
      await startService(id);
      message.success('服务已开始');
      fetchData(pagination.current, pagination.pageSize);
    } catch {
      message.error('操作失败');
    }
  };

  const handleComplete = async (id: string) => {
    try {
      await completeService(id);
      message.success('服务已完成');
      fetchData(pagination.current, pagination.pageSize);
    } catch {
      message.error('操作失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingOrder) {
        await updateOrder(editingOrder.id, values);
        message.success('更新成功');
      } else {
        await createOrder(values);
        message.success('创建成功');
      }
      setModalVisible(false);
      fetchData(pagination.current, pagination.pageSize);
    } catch {
      message.error('操作失败');
    }
  };

  const getStatusColor = (status: number): string => {
    const colorMap: Record<number, string> = {
      0: 'default',
      1: 'orange',
      2: 'blue',
      3: 'cyan',
      4: 'purple',
      5: 'gold',
      6: 'green',
    };
    return colorMap[status] || 'default';
  };

  const columns: ColumnsType<Order> = [
    { title: '订单号', dataIndex: 'orderNo', key: 'orderNo' },
    { title: '老人姓名', dataIndex: 'elderName', key: 'elderName' },
    { title: '服务类型', dataIndex: 'serviceType', key: 'serviceType' },
    { title: '服务日期', dataIndex: 'serviceTime', key: 'serviceTime', render: (text) => formatDate(text, 'YYYY-MM-DD') },
    { title: '服务人员', dataIndex: 'staffName', key: 'staffName', render: (name) => name || '-' },
    { title: '服务商', dataIndex: 'providerName', key: 'providerName' },
    {
      title: '订单状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => (
        <Tag color={getStatusColor(status)}>{getOrderStatusText(status)}</Tag>
      ),
    },
    {
      title: '服务费',
      dataIndex: 'serviceFee',
      key: 'serviceFee',
      render: (fee: number) => formatCurrency(fee),
    },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime', render: (text) => formatDate(text) },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          {record.status === 2 && (
            <Button type="link" size="small" icon={<PlayCircleOutlined />} onClick={() => handleStart(record.id)}>
              开始服务
            </Button>
          )}
          {record.status === 4 && (
            <Button type="link" size="small" icon={<CheckCircleOutlined />} onClick={() => handleComplete(record.id)}>
              完成服务
            </Button>
          )}
          {record.status < 4 && (
            <Button type="link" size="small" danger icon={<StopOutlined />} onClick={() => handleCancel(record.id)}>
              取消
            </Button>
          )}
          <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)}>
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <Card
      title="订单管理"
      extra={
        <Space>
          <Button icon={<ReloadOutlined />} onClick={() => fetchData(pagination.current, pagination.pageSize)}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增订单
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

      <Modal
        title={editingOrder ? '编辑订单' : '新增订单'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="elderName" label="老人姓名" rules={[{ required: true, message: '请输入老人姓名' }]}>
            <Input placeholder="请输入老人姓名" />
          </Form.Item>
          <Form.Item name="serviceType" label="服务类型" rules={[{ required: true, message: '请输入服务类型' }]}>
            <Input placeholder="请输入服务类型" />
          </Form.Item>
          <Form.Item name="serviceTime" label="服务时间" rules={[{ required: true, message: '请选择服务时间' }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="serviceFee" label="服务费" rules={[{ required: true, message: '请输入服务费' }]}>
            <Input type="number" placeholder="请输入服务费" />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default OrderList;
